package com.mengzhihua.crm.service.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.SerialNumberGenerator;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.enums.CaseOrigin;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.service.FieldHistoryService;
import com.mengzhihua.crm.service.dto.CaseSurveyRequest;
import com.mengzhihua.crm.service.entity.AssignmentRule;
import com.mengzhihua.crm.service.entity.CaseArticleLink;
import com.mengzhihua.crm.service.dto.CaseCommentRequest;
import com.mengzhihua.crm.service.dto.CaseAssignRequest;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.entity.CaseComment;
import com.mengzhihua.crm.service.entity.CaseSurvey;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.entity.SlaPolicy;
import com.mengzhihua.crm.service.repository.AssignmentRuleRepository;
import com.mengzhihua.crm.service.repository.CaseArticleLinkRepository;
import com.mengzhihua.crm.service.repository.CaseCommentRepository;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.repository.CaseSurveyRepository;
import com.mengzhihua.crm.service.repository.SlaPolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CaseService {
    private static final Map<CaseStatus, Set<CaseStatus>> ALLOWED_TRANSITIONS =
            createTransitions();

    private final CrmCaseRepository caseRepository;
    private final CaseCommentRepository commentRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final AssignmentRuleRepository assignmentRuleRepository;
    private final CaseSurveyRepository surveyRepository;
    private final CaseArticleLinkRepository articleLinkRepository;
    private final FieldHistoryService fieldHistoryService;

    public CaseService(
            CrmCaseRepository caseRepository,
            CaseCommentRepository commentRepository
            , SlaPolicyRepository slaPolicyRepository,
            AssignmentRuleRepository assignmentRuleRepository,
            CaseSurveyRepository surveyRepository,
            CaseArticleLinkRepository articleLinkRepository,
            FieldHistoryService fieldHistoryService
    ) {
        this.caseRepository = caseRepository;
        this.commentRepository = commentRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.assignmentRuleRepository = assignmentRuleRepository;
        this.surveyRepository = surveyRepository;
        this.articleLinkRepository = articleLinkRepository;
        this.fieldHistoryService = fieldHistoryService;
    }

    private static Map<CaseStatus, Set<CaseStatus>> createTransitions() {
        Map<CaseStatus, Set<CaseStatus>> transitions =
                new EnumMap<>(CaseStatus.class);
        transitions.put(
                CaseStatus.NEW,
                EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.ESCALATED)
        );
        transitions.put(
                CaseStatus.IN_PROGRESS,
                EnumSet.of(
                        CaseStatus.PENDING_CUSTOMER,
                        CaseStatus.ESCALATED,
                        CaseStatus.RESOLVED
                )
        );
        transitions.put(
                CaseStatus.PENDING_CUSTOMER,
                EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.RESOLVED)
        );
        transitions.put(
                CaseStatus.ESCALATED,
                EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.RESOLVED)
        );
        transitions.put(
                CaseStatus.RESOLVED,
                EnumSet.of(CaseStatus.CLOSED, CaseStatus.IN_PROGRESS)
        );
        transitions.put(CaseStatus.CLOSED, EnumSet.noneOf(CaseStatus.class));
        return transitions;
    }

    public PageResult<CrmCase> list(
            int page,
            int size,
            String keyword,
            CaseStatus status,
            CasePriority priority,
            Long accountId,
            Boolean overdue
    ) {
        Specification<CrmCase> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("subject")), value),
                        builder.like(builder.lower(root.get("caseNo")), value)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(builder.equal(root.get("priority"), priority));
            }
            if (accountId != null) {
                predicates.add(builder.equal(root.get("accountId"), accountId));
            }
            if (Boolean.TRUE.equals(overdue)) {
                predicates.add(builder.lessThan(
                        root.get("slaDueAt"),
                        LocalDateTime.now()
                ));
                predicates.add(builder.notEqual(
                        root.get("status"),
                        CaseStatus.RESOLVED
                ));
                predicates.add(builder.notEqual(
                        root.get("status"),
                        CaseStatus.CLOSED
                ));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<CrmCase> result = caseRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (CrmCase) item);
    }

    public CrmCase get(Long id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new BizException("工单不存在"));
    }

    public CrmCase save(CrmCase crmCase) {
        if (crmCase.getStatus() == null) {
            crmCase.setStatus(CaseStatus.NEW);
        }
        if (crmCase.getPriority() == null) {
            crmCase.setPriority(CasePriority.MEDIUM);
        }
        if (crmCase.getOrigin() == null) {
            crmCase.setOrigin(CaseOrigin.WEB);
        }
        if (crmCase.getType() == null) {
            crmCase.setType(CaseType.PROBLEM);
        }
        if (crmCase.getEscalated() == null) {
            crmCase.setEscalated(false);
        }
        if (crmCase.getCaseNo() == null) {
            crmCase.setCaseNo(nextNo());
        }
        if (crmCase.getOwner() == null || crmCase.getOwner().trim().isEmpty()) {
            crmCase.setOwner(resolveOwner(crmCase));
        }
        if (crmCase.getSlaDueAt() == null) {
            SlaPolicy policy = slaPolicyRepository
                    .findFirstByPriorityAndActiveTrue(crmCase.getPriority());
            int resolveHours = policy == null
                    ? crmCase.getPriority().slaHours()
                    : policy.getResolveHours() == null
                    ? crmCase.getPriority().slaHours()
                    : policy.getResolveHours();
            int responseHours = policy == null
                    ? Math.max(1, resolveHours / 4)
                    : policy.getResponseHours() == null
                    ? Math.max(1, resolveHours / 4)
                    : policy.getResponseHours();
            crmCase.setSlaDueAt(LocalDateTime.now().plusHours(resolveHours));
            crmCase.setResponseDueAt(
                    LocalDateTime.now().plusHours(responseHours)
            );
        }
        return caseRepository.save(crmCase);
    }

    private String nextNo() {
        String prefix = "CS" + LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
        CrmCase latest = caseRepository
                .findTopByCaseNoStartingWithOrderByCaseNoDesc(prefix);
        return SerialNumberGenerator.next(
                prefix,
                latest == null ? null : latest.getCaseNo()
        );
    }

    public void delete(Long id) {
        caseRepository.deleteById(id);
    }

    public CrmCase changeStatus(Long id, CaseStatusRequest request) {
        CrmCase crmCase = get(id);
        CaseStatus oldStatus = crmCase.getStatus();
        Set<CaseStatus> targets = ALLOWED_TRANSITIONS.get(crmCase.getStatus());
        if (targets == null || !targets.contains(request.getStatus())) {
            throw new BizException("非法的工单状态流转");
        }
        if (request.getStatus() == CaseStatus.RESOLVED
                && (request.getSolution() == null
                || request.getSolution().trim().isEmpty())) {
            throw new BizException("解决工单必须填写解决方案");
        }
        crmCase.setStatus(request.getStatus());
        if (crmCase.getFirstResponseAt() == null
                && request.getStatus() != CaseStatus.NEW) {
            crmCase.setFirstResponseAt(LocalDateTime.now());
        }
        if (request.getSolution() != null) {
            crmCase.setSolution(request.getSolution());
        }
        if (request.getStatus() == CaseStatus.RESOLVED) {
            crmCase.setResolvedAt(LocalDateTime.now());
        }
        if (request.getStatus() == CaseStatus.CLOSED) {
            crmCase.setClosedAt(LocalDateTime.now());
        }
        CrmCase saved = caseRepository.save(crmCase);
        fieldHistoryService.record(
                RelatedType.CASE,
                id,
                "status",
                oldStatus,
                saved.getStatus()
        );
        return saved;
    }

    public CrmCase escalate(Long id) {
        CrmCase crmCase = get(id);
        CasePriority oldPriority = crmCase.getPriority();
        if (crmCase.getStatus() == CaseStatus.CLOSED) {
            throw new BizException("已关闭工单不能升级");
        }
        crmCase.setStatus(CaseStatus.ESCALATED);
        crmCase.setEscalated(true);
        crmCase.setPriority(crmCase.getPriority().next());
        CrmCase saved = caseRepository.save(crmCase);
        fieldHistoryService.record(
                RelatedType.CASE,
                id,
                "priority",
                oldPriority,
                saved.getPriority()
        );
        return saved;
    }

    public CrmCase assign(Long id, CaseAssignRequest request) {
        CrmCase crmCase = get(id);
        String oldOwner = crmCase.getOwner();
        crmCase.setOwner(
                request.getOwner() == null || request.getOwner().trim().isEmpty()
                        ? CurrentUser.usernameOrDefault()
                        : request.getOwner()
        );
        CrmCase saved = caseRepository.save(crmCase);
        fieldHistoryService.record(
                RelatedType.CASE,
                id,
                "owner",
                oldOwner,
                saved.getOwner()
        );
        return saved;
    }

    public List<CaseComment> comments(Long id) {
        return commentRepository.findByCaseIdOrderByCreatedAtAsc(id);
    }

    public CaseComment addComment(Long id, CaseCommentRequest request) {
        CrmCase crmCase = get(id);
        CaseComment comment = new CaseComment();
        comment.setCaseId(id);
        comment.setAuthor(
                request.getAuthor() == null
                        ? CurrentUser.usernameOrDefault()
                        : request.getAuthor()
        );
        comment.setContent(request.getContent());
        comment.setInternal(Boolean.TRUE.equals(request.getInternal()));
        if (!comment.getInternal() && crmCase.getFirstResponseAt() == null) {
            crmCase.setFirstResponseAt(LocalDateTime.now());
            caseRepository.save(crmCase);
        }
        return commentRepository.save(comment);
    }

    public CaseSurvey survey(Long id, CaseSurveyRequest request) {
        CrmCase crmCase = get(id);
        if (crmCase.getStatus() != CaseStatus.RESOLVED
                && crmCase.getStatus() != CaseStatus.CLOSED) {
            throw new BizException("只有已解决或已关闭工单可以评价");
        }
        if (surveyRepository.findByCaseId(id).isPresent()) {
            throw new BizException("该工单已评价");
        }
        CaseSurvey survey = new CaseSurvey();
        survey.setCaseId(id);
        survey.setScore(request.getScore());
        survey.setComment(request.getComment());
        crmCase.setSatisfactionScore(request.getScore());
        caseRepository.save(crmCase);
        return surveyRepository.save(survey);
    }

    public List<CaseArticleLink> articles(Long id) {
        get(id);
        return articleLinkRepository.findByCaseId(id);
    }

    public CaseArticleLink addArticle(Long id, Long articleId) {
        get(id);
        return articleLinkRepository
                .findByCaseIdAndArticleId(id, articleId)
                .orElseGet(() -> {
                    CaseArticleLink link = new CaseArticleLink();
                    link.setCaseId(id);
                    link.setArticleId(articleId);
                    return articleLinkRepository.save(link);
                });
    }

    public void removeArticle(Long id, Long articleId) {
        CaseArticleLink link = articleLinkRepository
                .findByCaseIdAndArticleId(id, articleId)
                .orElseThrow(() -> new BizException("工单关联文章不存在"));
        articleLinkRepository.delete(link);
    }

    @Transactional
    @Scheduled(fixedDelay = 300000)
    public void checkSla() {
        LocalDateTime now = LocalDateTime.now();
        List<CrmCase> cases = caseRepository.findAll();
        for (CrmCase crmCase : cases) {
            if (crmCase.getSlaDueAt() != null
                    && crmCase.getSlaDueAt().isBefore(now)
                    && crmCase.getStatus() != CaseStatus.RESOLVED
                    && crmCase.getStatus() != CaseStatus.CLOSED
                    && !Boolean.TRUE.equals(crmCase.getEscalated())) {
                CaseStatus oldStatus = crmCase.getStatus();
                crmCase.setStatus(CaseStatus.ESCALATED);
                crmCase.setEscalated(true);
                CasePriority oldPriority = crmCase.getPriority();
                crmCase.setPriority(oldPriority.next());
                caseRepository.save(crmCase);
                fieldHistoryService.record(
                        RelatedType.CASE,
                        crmCase.getId(),
                        "status",
                        oldStatus,
                        crmCase.getStatus()
                );
                fieldHistoryService.record(
                        RelatedType.CASE,
                        crmCase.getId(),
                        "priority",
                        oldPriority,
                        crmCase.getPriority()
                );
                CaseComment comment = new CaseComment();
                comment.setCaseId(crmCase.getId());
                comment.setAuthor("系统");
                comment.setContent("SLA 超期自动升级");
                comment.setInternal(true);
                commentRepository.save(comment);
            }
        }
    }

    private String resolveOwner(CrmCase crmCase) {
        for (AssignmentRule rule : assignmentRuleRepository
                .findByActiveTrueOrderByPriorityAsc()) {
            if (rule.getCaseType() != null
                    && rule.getCaseType() != crmCase.getType()) {
                continue;
            }
            if (rule.getCasePriority() != null
                    && rule.getCasePriority() != crmCase.getPriority()) {
                continue;
            }
            if (rule.getOrigin() != null
                    && rule.getOrigin() != crmCase.getOrigin()) {
                continue;
            }
            if (rule.getKeyword() != null
                    && (crmCase.getSubject() == null
                    || !crmCase.getSubject().contains(rule.getKeyword()))) {
                continue;
            }
            return rule.getAssignTo();
        }
        return "service";
    }
}
