package com.mengzhihua.crm.forecast.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.forecast.entity.SalesTarget;
import com.mengzhihua.crm.forecast.repository.SalesTargetRepository;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class ForecastService {
    private final SalesTargetRepository targetRepository;
    private final OpportunityRepository opportunityRepository;

    public ForecastService(
            SalesTargetRepository targetRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.targetRepository = targetRepository;
        this.opportunityRepository = opportunityRepository;
    }

    public PageResult<SalesTarget> targets(
            int page,
            int size,
            Integer year,
            Integer month,
            String owner
    ) {
        Specification<SalesTarget> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (year != null) {
                predicates.add(builder.equal(root.get("year"), year));
            }
            if (month != null) {
                predicates.add(builder.equal(root.get("month"), month));
            }
            if (owner != null && !owner.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("owner"), owner));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<SalesTarget> result = targetRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (SalesTarget) item);
    }

    public SalesTarget get(Long id) {
        return targetRepository.findById(id)
                .orElseThrow(() -> new BizException("销售目标不存在"));
    }

    public SalesTarget save(SalesTarget target) {
        if (target.getOwner() == null || target.getOwner().trim().isEmpty()) {
            target.setOwner(CurrentUser.usernameOrDefault());
        }
        if (target.getTargetAmount() == null) {
            target.setTargetAmount(BigDecimal.ZERO);
        }
        SalesTarget existing = targetRepository
                .findByOwnerAndYearAndMonth(
                        target.getOwner(),
                        target.getYear(),
                        target.getMonth()
                )
                .orElse(null);
        if (existing != null
                && (target.getId() == null
                || !existing.getId().equals(target.getId()))) {
            throw new BizException("该销售目标已存在");
        }
        return targetRepository.save(target);
    }

    public void delete(Long id) {
        targetRepository.deleteById(id);
    }

    public List<Map<String, Object>> forecast(
            int year,
            int month,
            String owner
    ) {
        Set<String> owners = new TreeSet<>();
        for (SalesTarget target : targetRepository.findByYearAndMonth(year, month)) {
            owners.add(target.getOwner());
        }
        for (Opportunity opportunity : opportunityRepository.findAll()) {
            if (ownerMatches(opportunity, owner)
                    && opportunity.getOwner() != null) {
                owners.add(opportunity.getOwner());
            }
        }
        if (owner != null && !owner.trim().isEmpty()) {
            owners.removeIf(item -> !item.equals(owner));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal totalTarget = BigDecimal.ZERO;
        BigDecimal totalWon = BigDecimal.ZERO;
        BigDecimal totalPipeline = BigDecimal.ZERO;
        BigDecimal totalWeighted = BigDecimal.ZERO;
        for (String itemOwner : owners) {
            Map<String, Object> row = calculate(itemOwner, year, month);
            result.add(row);
            totalTarget = totalTarget.add((BigDecimal) row.get("targetAmount"));
            totalWon = totalWon.add((BigDecimal) row.get("wonAmount"));
            totalPipeline = totalPipeline.add(
                    (BigDecimal) row.get("pipelineAmount")
            );
            totalWeighted = totalWeighted.add(
                    (BigDecimal) row.get("weightedAmount")
            );
        }
        if (owner == null || owner.trim().isEmpty()) {
            result.add(summaryRow(
                    totalTarget,
                    totalWon,
                    totalPipeline,
                    totalWeighted
            ));
        }
        return result;
    }

    public List<Map<String, Object>> trend(int year) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<Map<String, Object>> rows = forecast(year, month, null);
            Map<String, Object> row = rows.isEmpty()
                    ? summaryRow(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            )
                    : rows.get(rows.size() - 1);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("targetAmount", row.get("targetAmount"));
            item.put("wonAmount", row.get("wonAmount"));
            item.put("weightedAmount", row.get("weightedAmount"));
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> currentSummary() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> rows = forecast(
                today.getYear(),
                today.getMonthValue(),
                null
        );
        return rows.isEmpty()
                ? summaryRow(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        )
                : rows.get(rows.size() - 1);
    }

    private Map<String, Object> calculate(
            String owner,
            int year,
            int month
    ) {
        BigDecimal target = targetRepository
                .findByOwnerAndYearAndMonth(owner, year, month)
                .map(SalesTarget::getTargetAmount)
                .orElse(BigDecimal.ZERO);
        BigDecimal won = BigDecimal.ZERO;
        BigDecimal pipeline = BigDecimal.ZERO;
        BigDecimal weighted = BigDecimal.ZERO;
        for (Opportunity opportunity : opportunityRepository.findAll()) {
            if (!owner.equals(opportunity.getOwner())) {
                continue;
            }
            BigDecimal amount = value(opportunity.getAmount());
            if (opportunity.getStage() == OpportunityStage.CLOSED_WON
                    && opportunity.getClosedAt() != null
                    && opportunity.getClosedAt().getYear() == year
                    && opportunity.getClosedAt().getMonthValue() == month) {
                won = won.add(amount);
            }
            if (opportunity.getStage() != OpportunityStage.CLOSED_WON
                    && opportunity.getStage() != OpportunityStage.CLOSED_LOST
                    && opportunity.getExpectedCloseDate() != null
                    && opportunity.getExpectedCloseDate().getYear() == year
                    && opportunity.getExpectedCloseDate().getMonthValue() == month) {
                pipeline = pipeline.add(amount);
                int probability = opportunity.getProbability() == null
                        ? 0
                        : opportunity.getProbability();
                weighted = weighted.add(amount.multiply(
                        BigDecimal.valueOf(probability)
                                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                ));
            }
        }
        return row(owner, target, won, pipeline, weighted);
    }

    private Map<String, Object> row(
            String owner,
            BigDecimal target,
            BigDecimal won,
            BigDecimal pipeline,
            BigDecimal weighted
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("owner", owner);
        result.put("targetAmount", target);
        result.put("wonAmount", won);
        result.put("pipelineAmount", pipeline);
        result.put("weightedAmount", weighted);
        BigDecimal achievement = target.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : won.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        result.put("achievementRate", achievement);
        result.put("gap", target.subtract(won));
        return result;
    }

    private Map<String, Object> summaryRow(
            BigDecimal target,
            BigDecimal won,
            BigDecimal pipeline,
            BigDecimal weighted
    ) {
        Map<String, Object> result = row(
                "合计",
                target,
                won,
                pipeline,
                weighted
        );
        return result;
    }

    private boolean ownerMatches(Opportunity opportunity, String owner) {
        return owner == null
                || owner.trim().isEmpty()
                || owner.equals(opportunity.getOwner());
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
