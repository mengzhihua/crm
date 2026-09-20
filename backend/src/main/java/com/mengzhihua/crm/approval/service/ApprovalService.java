package com.mengzhihua.crm.approval.service;

import com.mengzhihua.crm.approval.dto.ApprovalDecision;
import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.approval.entity.ApprovalStep;
import com.mengzhihua.crm.approval.repository.ApprovalRequestRepository;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.approval.repository.ApprovalStepRepository;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.common.enums.NotificationType;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.notification.service.NotificationService;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.repository.QuoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApprovalService {
    private final ApprovalRuleRepository ruleRepository;
    private final ApprovalRequestRepository requestRepository;
    private final QuoteRepository quoteRepository;
    private final ApprovalStepRepository stepRepository;
    private final NotificationService notificationService;

    public ApprovalService(
            ApprovalRuleRepository ruleRepository,
            ApprovalRequestRepository requestRepository,
            QuoteRepository quoteRepository,
            ApprovalStepRepository stepRepository,
            NotificationService notificationService
    ) {
        this.ruleRepository = ruleRepository;
        this.requestRepository = requestRepository;
        this.quoteRepository = quoteRepository;
        this.stepRepository = stepRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public boolean submit(
            ApprovalTargetType targetType,
            Long targetId,
            BigDecimal amount,
            BigDecimal discountRate,
            String title
    ) {
        ApprovalRule matched = match(
                targetType,
                amount,
                discountRate
        );
        if (matched == null) {
            return false;
        }
        List<Role> roles = matched.roles();
        if (roles.isEmpty()) {
            throw new BizException("审批规则未配置审批角色");
        }
        ApprovalRequest request = new ApprovalRequest();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setTitle(title);
        request.setSubmitter(CurrentUser.usernameOrDefault());
        request.setApproverRole(roles.get(0));
        request.setCurrentStep(1);
        request.setTotalSteps(roles.size());
        request.setStatus(ApprovalStatus.PENDING);
        request.setSubmittedAt(LocalDateTime.now());
        request = requestRepository.save(request);
        for (int i = 0; i < roles.size(); i++) {
            ApprovalStep step = new ApprovalStep();
            step.setRequestId(request.getId());
            step.setStepOrder(i + 1);
            step.setApproverRole(roles.get(i));
            stepRepository.save(step);
        }
        notificationService.sendToRole(
                roles.get(0),
                NotificationType.APPROVAL,
                "待审批：" + title,
                "报价审批已提交，请处理第 1 级审批",
                RelatedType.QUOTE,
                targetId
        );
        return true;
    }

    public ApprovalRule match(
            ApprovalTargetType targetType,
            BigDecimal amount,
            BigDecimal discountRate
    ) {
        List<ApprovalRule> rules = ruleRepository
                .findByTargetTypeAndActiveTrueOrderByPriorityAsc(targetType);
        for (ApprovalRule rule : rules) {
            boolean amountMatched = rule.getMinAmount() != null
                    && amount != null
                    && amount.compareTo(rule.getMinAmount()) >= 0;
            boolean discountMatched = rule.getMinDiscountRate() != null
                    && discountRate != null
                    && discountRate.compareTo(rule.getMinDiscountRate()) >= 0;
            if (amountMatched || discountMatched) {
                return rule;
            }
        }
        return null;
    }

    public PageResult<ApprovalRequest> list(
            int page,
            int size,
            ApprovalStatus status,
            boolean mine
    ) {
        Role role = CurrentUser.role();
        Specification<ApprovalRequest> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (mine && role != Role.ADMIN) {
                predicates.add(builder.equal(root.get("approverRole"), role));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<ApprovalRequest> result = requestRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (ApprovalRequest) item);
    }

    @Transactional
    public ApprovalRequest decide(
            Long id,
            boolean approved,
            ApprovalDecision decision
    ) {
        ApprovalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new BizException("审批申请不存在"));
        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BizException("审批申请已处理");
        }
        Role role = CurrentUser.role();
        if (role != Role.ADMIN && role != request.getApproverRole()) {
            throw new BizException("当前角色无权处理该审批");
        }
        ApprovalStep step = stepRepository
                .findByRequestIdOrderByStepOrderAsc(request.getId())
                .stream()
                .filter(item -> item.getStepOrder() == request.getCurrentStep())
                .findFirst()
                .orElseThrow(() -> new BizException("审批步骤不存在"));
        step.setStatus(approved
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.REJECTED);
        step.setApprover(CurrentUser.usernameOrDefault());
        step.setComment(decision == null ? null : decision.getComment());
        step.setDecidedAt(LocalDateTime.now());
        stepRepository.save(step);
        if (!approved) {
            request.setStatus(ApprovalStatus.REJECTED);
            request.setApprover(CurrentUser.usernameOrDefault());
            request.setComment(decision == null ? null : decision.getComment());
            request.setDecidedAt(LocalDateTime.now());
            requestRepository.save(request);
            finalizeQuote(request, QuoteStatus.REJECTED);
            notificationService.send(
                    request.getSubmitter(),
                    NotificationType.APPROVAL,
                    "审批拒绝：" + request.getTitle(),
                    "审批申请已被拒绝",
                    RelatedType.QUOTE,
                    request.getTargetId()
            );
            return request;
        }
        if (request.getCurrentStep() < request.getTotalSteps()) {
            request.setCurrentStep(request.getCurrentStep() + 1);
            Role nextRole = stepRepository
                    .findByRequestIdOrderByStepOrderAsc(request.getId())
                    .stream()
                    .filter(item -> item.getStepOrder() == request.getCurrentStep())
                    .map(ApprovalStep::getApproverRole)
                    .findFirst()
                    .orElseThrow(() -> new BizException("下一审批步骤不存在"));
            request.setApproverRole(nextRole);
            requestRepository.save(request);
            notificationService.sendToRole(
                    nextRole,
                    NotificationType.APPROVAL,
                    "待审批：" + request.getTitle(),
                    "审批已进入第 " + request.getCurrentStep() + " 级",
                    RelatedType.QUOTE,
                    request.getTargetId()
            );
            return request;
        }
        request.setStatus(ApprovalStatus.APPROVED);
        request.setApprover(CurrentUser.usernameOrDefault());
        request.setComment(decision == null ? null : decision.getComment());
        request.setDecidedAt(LocalDateTime.now());
        requestRepository.save(request);
        finalizeQuote(request, QuoteStatus.APPROVED);
        notificationService.send(
                request.getSubmitter(),
                NotificationType.APPROVAL,
                "审批通过：" + request.getTitle(),
                "审批申请已全部通过",
                RelatedType.QUOTE,
                request.getTargetId()
        );
        return request;
    }

    public List<ApprovalStep> steps(Long requestId) {
        requestRepository.findById(requestId)
                .orElseThrow(() -> new BizException("审批申请不存在"));
        return stepRepository.findByRequestIdOrderByStepOrderAsc(requestId);
    }

    public List<ApprovalRule> rules() {
        return ruleRepository.findAll();
    }

    public ApprovalRule saveRule(ApprovalRule rule) {
        validateRule(rule);
        if (rule.getId() != null) {
            ApprovalRule current = ruleRepository.findById(rule.getId())
                    .orElseThrow(() -> new BizException("审批规则不存在"));
            rule.setOwner(current.getOwner());
        }
        return ruleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    private void validateRule(ApprovalRule rule) {
        if (rule.getApproverRoles() == null
                || rule.getApproverRoles().trim().isEmpty()) {
            throw new BizException("审批角色不能为空");
        }
        try {
            rule.roles();
        } catch (IllegalArgumentException exception) {
            throw new BizException("审批角色无效");
        }
    }

    private void finalizeQuote(
            ApprovalRequest request,
            QuoteStatus status
    ) {
        if (request.getTargetType() != ApprovalTargetType.QUOTE) {
            return;
        }
        Quote quote = quoteRepository.findById(request.getTargetId())
                .orElseThrow(() -> new BizException("报价单不存在"));
        quote.setStatus(status);
        quoteRepository.save(quote);
    }
}
