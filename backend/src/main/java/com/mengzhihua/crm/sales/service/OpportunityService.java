package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.service.FieldHistoryService;
import com.mengzhihua.crm.sales.dto.OpportunityStageRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunityRepository;
    private final FieldHistoryService fieldHistoryService;

    public OpportunityService(
            OpportunityRepository opportunityRepository,
            FieldHistoryService fieldHistoryService
    ) {
        this.opportunityRepository = opportunityRepository;
        this.fieldHistoryService = fieldHistoryService;
    }

    public PageResult<Opportunity> list(
            int page,
            int size,
            String keyword,
            Long accountId,
            OpportunityStage stage
    ) {
        Specification<Opportunity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            if (accountId != null) {
                predicates.add(builder.equal(root.get("accountId"), accountId));
            }
            if (stage != null) {
                predicates.add(builder.equal(root.get("stage"), stage));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Opportunity> result = opportunityRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Opportunity) item);
    }

    public Opportunity get(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new BizException("商机不存在"));
    }

    public Opportunity save(Opportunity opportunity) {
        if (opportunity.getStage() == null) {
            opportunity.setStage(OpportunityStage.QUALIFICATION);
        }
        if (opportunity.getProbability() == null) {
            opportunity.setProbability(
                    opportunity.getStage().getDefaultProbability()
            );
        }
        return opportunityRepository.save(opportunity);
    }

    public void delete(Long id) {
        opportunityRepository.deleteById(id);
    }

    public Opportunity changeStage(Long id, OpportunityStageRequest request) {
        Opportunity opportunity = get(id);
        OpportunityStage oldStage = opportunity.getStage();
        BigDecimal oldAmount = opportunity.getAmount();
        if (request.getStage() == OpportunityStage.CLOSED_LOST
                && (request.getLostReason() == null
                || request.getLostReason().trim().isEmpty())) {
            throw new BizException("丢单必须填写原因");
        }
        opportunity.setStage(request.getStage());
        opportunity.setProbability(
                request.getProbability() == null
                        ? request.getStage().getDefaultProbability()
                        : request.getProbability()
        );
        if (request.getLostReason() != null) {
            opportunity.setLostReason(request.getLostReason());
        }
        if (request.getStage() == OpportunityStage.CLOSED_WON
                || request.getStage() == OpportunityStage.CLOSED_LOST) {
            opportunity.setClosedAt(LocalDateTime.now());
        }
        Opportunity saved = opportunityRepository.save(opportunity);
        fieldHistoryService.record(
                RelatedType.OPPORTUNITY,
                id,
                "stage",
                oldStage,
                saved.getStage()
        );
        fieldHistoryService.record(
                RelatedType.OPPORTUNITY,
                id,
                "amount",
                oldAmount,
                saved.getAmount()
        );
        return saved;
    }

    public List<Map<String, Object>> pipeline() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            List<Opportunity> opportunities = opportunityRepository.findByStage(stage);
            BigDecimal amount = opportunities.stream()
                    .map(Opportunity::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stage", stage);
            item.put("count", opportunities.size());
            item.put("sumAmount", amount);
            result.add(item);
        }
        return result;
    }
}
