package com.mengzhihua.crm.approval.service;

import com.mengzhihua.crm.approval.dto.ApprovalDecision;
import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.approval.repository.ApprovalRequestRepository;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.common.enums.Role;
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

    public ApprovalService(
            ApprovalRuleRepository ruleRepository,
            ApprovalRequestRepository requestRepository,
            QuoteRepository quoteRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.requestRepository = requestRepository;
        this.quoteRepository = quoteRepository;
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
        ApprovalRequest request = new ApprovalRequest();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setTitle(title);
        request.setSubmitter(CurrentUser.usernameOrDefault());
        request.setApproverRole(matched.getApproverRole());
        request.setStatus(ApprovalStatus.PENDING);
        request.setSubmittedAt(LocalDateTime.now());
        requestRepository.save(request);
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
        request.setStatus(approved
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.REJECTED);
        request.setApprover(CurrentUser.usernameOrDefault());
        request.setComment(decision == null ? null : decision.getComment());
        request.setDecidedAt(LocalDateTime.now());
        requestRepository.save(request);
        if (request.getTargetType() == ApprovalTargetType.QUOTE) {
            Quote quote = quoteRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new BizException("报价单不存在"));
            quote.setStatus(approved
                    ? QuoteStatus.APPROVED
                    : QuoteStatus.REJECTED);
            quoteRepository.save(quote);
        }
        return request;
    }
}
