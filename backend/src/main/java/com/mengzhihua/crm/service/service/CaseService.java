package com.mengzhihua.crm.service.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.CaseOrigin;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.service.dto.CaseCommentRequest;
import com.mengzhihua.crm.service.dto.CaseAssignRequest;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.entity.CaseComment;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CaseCommentRepository;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

    public CaseService(
            CrmCaseRepository caseRepository,
            CaseCommentRepository commentRepository
    ) {
        this.caseRepository = caseRepository;
        this.commentRepository = commentRepository;
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
        if (crmCase.getSlaDueAt() == null) {
            crmCase.setSlaDueAt(
                    LocalDateTime.now().plusHours(
                            crmCase.getPriority().slaHours()
                    )
            );
        }
        return caseRepository.save(crmCase);
    }

    private String nextNo() {
        String prefix = "CS" + LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
        CrmCase latest = caseRepository
                .findTopByCaseNoStartingWithOrderByCaseNoDesc(prefix);
        int sequence = 1;
        if (latest != null && latest.getCaseNo().length() >= 12) {
            String suffix = latest.getCaseNo().substring(10);
            sequence = Integer.parseInt(suffix) + 1;
        }
        return prefix + String.format("%04d", sequence);
    }

    public void delete(Long id) {
        caseRepository.deleteById(id);
    }

    public CrmCase changeStatus(Long id, CaseStatusRequest request) {
        CrmCase crmCase = get(id);
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
        if (request.getSolution() != null) {
            crmCase.setSolution(request.getSolution());
        }
        if (request.getStatus() == CaseStatus.RESOLVED) {
            crmCase.setResolvedAt(LocalDateTime.now());
        }
        if (request.getStatus() == CaseStatus.CLOSED) {
            crmCase.setClosedAt(LocalDateTime.now());
        }
        return caseRepository.save(crmCase);
    }

    public CrmCase escalate(Long id) {
        CrmCase crmCase = get(id);
        if (crmCase.getStatus() == CaseStatus.CLOSED) {
            throw new BizException("已关闭工单不能升级");
        }
        crmCase.setStatus(CaseStatus.ESCALATED);
        crmCase.setEscalated(true);
        crmCase.setPriority(crmCase.getPriority().next());
        return caseRepository.save(crmCase);
    }

    public CrmCase assign(Long id, CaseAssignRequest request) {
        CrmCase crmCase = get(id);
        crmCase.setOwner(request.getOwner());
        return caseRepository.save(crmCase);
    }

    public List<CaseComment> comments(Long id) {
        return commentRepository.findByCaseIdOrderByCreatedAtAsc(id);
    }

    public CaseComment addComment(Long id, CaseCommentRequest request) {
        CaseComment comment = new CaseComment();
        comment.setCaseId(id);
        comment.setAuthor(request.getAuthor());
        comment.setContent(request.getContent());
        comment.setInternal(Boolean.TRUE.equals(request.getInternal()));
        return commentRepository.save(comment);
    }
}
