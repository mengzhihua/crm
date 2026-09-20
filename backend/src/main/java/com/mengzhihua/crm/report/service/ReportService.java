package com.mengzhihua.crm.report.service;

import com.mengzhihua.crm.common.CsvExportService;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.MemberType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.entity.PaymentPlan;
import com.mengzhihua.crm.contract.entity.PaymentRecord;
import com.mengzhihua.crm.contract.repository.ContractRepository;
import com.mengzhihua.crm.contract.repository.PaymentPlanRepository;
import com.mengzhihua.crm.contract.repository.PaymentRecordRepository;
import com.mengzhihua.crm.forecast.entity.SalesTarget;
import com.mengzhihua.crm.forecast.repository.SalesTargetRepository;
import com.mengzhihua.crm.marketing.entity.Campaign;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import com.mengzhihua.crm.marketing.repository.CampaignMemberRepository;
import com.mengzhihua.crm.marketing.repository.CampaignRepository;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final CrmCaseRepository caseRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository memberRepository;
    private final ContractRepository contractRepository;
    private final PaymentPlanRepository planRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    public ReportService(
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            SalesTargetRepository salesTargetRepository,
            CrmCaseRepository caseRepository,
            CampaignRepository campaignRepository,
            CampaignMemberRepository memberRepository,
            ContractRepository contractRepository,
            PaymentPlanRepository planRepository,
            PaymentRecordRepository paymentRecordRepository
    ) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.caseRepository = caseRepository;
        this.campaignRepository = campaignRepository;
        this.memberRepository = memberRepository;
        this.contractRepository = contractRepository;
        this.planRepository = planRepository;
        this.paymentRecordRepository = paymentRecordRepository;
    }

    public Map<String, Object> salesFunnel() {
        List<Lead> leads = leadRepository.findAll();
        List<Opportunity> opportunities = opportunityRepository.findAll();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("leads", group(leads, item -> item.getStatus() == null
                ? null : item.getStatus().name()));
        List<Map<String, Object>> opportunityGroups = new ArrayList<>();
        opportunities.stream()
                .filter(item -> item.getStage() != null)
                .collect(Collectors.groupingBy(Opportunity::getStage))
                .forEach((stage, items) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("stage", stage == null ? null : stage.name());
                    row.put("count", items.size());
                    row.put("amount", sum(items, Opportunity::getAmount));
                    opportunityGroups.add(row);
                });
        result.put("opportunities", opportunityGroups);
        Map<String, Object> conversion = new LinkedHashMap<>();
        long won = opportunities.stream()
                .filter(item -> item.getStage() == OpportunityStage.CLOSED_WON)
                .count();
        conversion.put("leadCount", leads.size());
        conversion.put("convertedLeadCount", leads.stream()
                .filter(item -> item.getStatus() == LeadStatus.CONVERTED)
                .count());
        conversion.put("opportunityCount", opportunities.size());
        conversion.put("wonCount", won);
        conversion.put("winRate", opportunities.isEmpty()
                ? BigDecimal.ZERO
                : percent(won, opportunities.size()));
        result.put("conversion", conversion);
        return result;
    }

    public List<Map<String, Object>> salesPerformance(int year, int month) {
        List<Opportunity> opportunities = opportunityRepository.findAll();
        Map<String, List<Opportunity>> wonByOwner = opportunities.stream()
                .filter(item -> item.getStage() == OpportunityStage.CLOSED_WON)
                .filter(item -> item.getClosedAt() != null
                        && item.getClosedAt().getYear() == year
                        && item.getClosedAt().getMonthValue() == month)
                .collect(Collectors.groupingBy(Opportunity::getOwner));
        List<SalesTarget> targets = salesTargetRepository.findAll().stream()
                .filter(item -> Objects.equals(item.getYear(), year)
                        && Objects.equals(item.getMonth(), month))
                .collect(Collectors.toList());
        Set<String> owners = new TreeSet<>();
        owners.addAll(wonByOwner.keySet());
        targets.stream().map(SalesTarget::getOwner).forEach(owners::add);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String owner : owners) {
            List<Opportunity> won = wonByOwner.getOrDefault(owner, Collections.emptyList());
            BigDecimal target = targets.stream()
                    .filter(item -> owner.equals(item.getOwner()))
                    .map(SalesTarget::getTargetAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal amount = sum(won, Opportunity::getAmount);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("owner", owner);
            row.put("wonCount", won.size());
            row.put("wonAmount", amount);
            row.put("targetAmount", target);
            row.put("attainmentRate", target.signum() == 0
                    ? BigDecimal.ZERO : amount.multiply(new BigDecimal("100"))
                    .divide(target, 2, RoundingMode.HALF_UP));
            result.add(row);
        }
        return result;
    }

    public Map<String, Object> caseAnalysis(LocalDate from, LocalDate to) {
        List<CrmCase> cases = caseRepository.findAll().stream()
                .filter(item -> item.getCreatedAt() != null)
                .filter(item -> !item.getCreatedAt().toLocalDate().isBefore(from)
                        && !item.getCreatedAt().toLocalDate().isAfter(to))
                .collect(Collectors.toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("byType", group(cases, item -> item.getType() == null
                ? null : item.getType().name()));
        result.put("byPriority", group(cases, item -> item.getPriority() == null
                ? null : item.getPriority().name()));
        result.put("byStatus", group(cases, item -> item.getStatus() == null
                ? null : item.getStatus().name()));
        List<CrmCase> resolved = cases.stream()
                .filter(item -> item.getStatus() == CaseStatus.RESOLVED
                        || item.getStatus() == CaseStatus.CLOSED)
                .collect(Collectors.toList());
        double resolveHours = resolved.stream()
                .filter(item -> item.getResolvedAt() != null)
                .mapToLong(item -> ChronoUnit.MINUTES.between(
                        item.getCreatedAt(),
                        item.getResolvedAt()
                ))
                .average().orElse(0D) / 60D;
        List<CrmCase> slaCases = resolved.stream()
                .filter(item -> item.getSlaDueAt() != null
                        && item.getResolvedAt() != null)
                .collect(Collectors.toList());
        long slaMet = slaCases.stream()
                .filter(item -> !item.getResolvedAt().isAfter(item.getSlaDueAt()))
                .count();
        double satisfaction = cases.stream()
                .filter(item -> item.getSatisfactionScore() != null)
                .mapToInt(CrmCase::getSatisfactionScore)
                .average().orElse(0D);
        result.put("avgResolveHours", round(resolveHours));
        result.put("slaMetRate", slaCases.isEmpty()
                ? BigDecimal.ZERO : percent(slaMet, slaCases.size()));
        result.put("avgSatisfaction", round(satisfaction));
        result.put("escalatedCount", cases.stream()
                .filter(item -> Boolean.TRUE.equals(item.getEscalated()))
                .count());
        return result;
    }

    public List<Map<String, Object>> campaignRoi() {
        List<Opportunity> opportunities = opportunityRepository.findAll();
        List<Lead> leads = leadRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Campaign campaign : campaignRepository.findAll()) {
            List<CampaignMember> members = memberRepository
                    .findByCampaignId(campaign.getId());
            List<Opportunity> campaignOpps = opportunities.stream()
                    .filter(item -> Objects.equals(item.getCampaignId(), campaign.getId()))
                    .collect(Collectors.toList());
            BigDecimal cost = defaultZero(campaign.getActualCost());
            BigDecimal wonAmount = sum(
                    campaignOpps.stream()
                            .filter(item -> item.getStage() == OpportunityStage.CLOSED_WON)
                            .collect(Collectors.toList()),
                    Opportunity::getAmount
            );
            long leadCount = leads.stream()
                    .filter(item -> Objects.equals(item.getCampaignId(), campaign.getId()))
                    .count();
            if (leadCount == 0) {
                leadCount = members.stream()
                        .filter(item -> item.getMemberType() == MemberType.LEAD)
                        .count();
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("campaignId", campaign.getId());
            row.put("name", campaign.getName());
            row.put("type", campaign.getType());
            row.put("status", campaign.getStatus());
            row.put("memberCount", members.size());
            row.put("leadCount", leadCount);
            row.put("opportunityCount", campaignOpps.size());
            row.put("wonAmount", wonAmount);
            row.put("actualCost", campaign.getActualCost());
            row.put("roi", cost.signum() == 0 ? null : wonAmount
                    .subtract(cost).multiply(new BigDecimal("100"))
                    .divide(cost, 2, RoundingMode.HALF_UP));
            result.add(row);
        }
        return result;
    }

    public Map<String, Object> receivables() {
        List<Contract> contracts = contractRepository.findAll();
        List<PaymentPlan> plans = planRepository.findAll();
        LocalDate today = LocalDate.now();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("contractCount", contracts.size());
        summary.put("totalAmount", sum(contracts, Contract::getAmount));
        summary.put("receivedAmount", sum(contracts, Contract::getReceivedAmount));
        summary.put("receivableAmount", sum(contracts, Contract::getReceivableAmount));
        List<PaymentPlan> overdue = plans.stream()
                .filter(item -> item.getPlanDate() != null
                        && item.getPlanDate().isBefore(today)
                        && item.getStatus() != com.mengzhihua.crm.common.enums.PaymentStatus.PAID)
                .collect(Collectors.toList());
        summary.put("overduePlanCount", overdue.size());
        summary.put("overdueAmount", overdue.stream()
                .map(item -> defaultZero(item.getPlanAmount())
                        .subtract(defaultZero(item.getPaidAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Map<String, Map<String, BigDecimal>> monthly = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (PaymentPlan plan : plans) {
            if (plan.getPlanDate() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(plan.getPlanDate());
            if (month.isBefore(current.minusMonths(11)) || month.isAfter(current)) {
                continue;
            }
            String key = month.toString();
            Map<String, BigDecimal> values = monthly.computeIfAbsent(
                    key,
                    item -> new LinkedHashMap<>()
            );
            values.put("planAmount", defaultZero(values.get("planAmount"))
                    .add(defaultZero(plan.getPlanAmount())));
            values.put("paidAmount", defaultZero(values.get("paidAmount"))
                    .add(defaultZero(plan.getPaidAmount())));
        }
        List<Map<String, Object>> monthlyRows = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            String key = current.minusMonths(i).toString();
            Map<String, BigDecimal> values = monthly.getOrDefault(
                    key,
                    Collections.emptyMap()
            );
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", key);
            row.put("planAmount", defaultZero(values.get("planAmount")));
            row.put("paidAmount", defaultZero(values.get("paidAmount")));
            monthlyRows.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("monthly", monthlyRows);
        return result;
    }

    public ResponseEntity<byte[]> export(String report, int year, int month) {
        List<List<?>> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        if ("sales-funnel".equals(report)) {
            headers = Arrays.asList("类型", "状态", "数量", "金额");
            salesFunnel().get("leads");
        } else if ("sales-performance".equals(report)) {
            headers = Arrays.asList("负责人", "赢单数", "赢单金额", "目标金额", "达成率");
            for (Map<String, Object> item : salesPerformance(year, month)) {
                rows.add(Arrays.asList(
                        item.get("owner"), item.get("wonCount"),
                        item.get("wonAmount"), item.get("targetAmount"),
                        item.get("attainmentRate")
                ));
            }
        } else if ("campaign-roi".equals(report)) {
            headers = Arrays.asList("活动", "成员数", "线索数", "商机数", "赢单金额",
                    "实际成本", "ROI");
            for (Map<String, Object> item : campaignRoi()) {
                rows.add(Arrays.asList(item.get("name"), item.get("memberCount"),
                        item.get("leadCount"), item.get("opportunityCount"),
                        item.get("wonAmount"), item.get("actualCost"), item.get("roi")));
            }
        } else {
            headers = Arrays.asList("指标", "数值");
            Map<String, Object> reportData = "receivables".equals(report)
                    ? receivables() : caseAnalysis(LocalDate.now().minusDays(30),
                    LocalDate.now());
            for (Map.Entry<String, Object> entry : reportData.entrySet()) {
                rows.add(Arrays.asList(entry.getKey(), entry.getValue()));
            }
        }
        return CsvExportService.download(report + ".csv", headers, rows);
    }

    private <T> List<Map<String, Object>> group(
            List<T> source,
            Function<T, String> keyFunction
    ) {
        return source.stream()
                .filter(item -> keyFunction.apply(item) != null)
                .collect(Collectors.groupingBy(
                        keyFunction,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("status", entry.getKey());
                    row.put("type", entry.getKey());
                    row.put("count", entry.getValue());
                    return row;
                })
                .sorted(Comparator.comparing(item -> String.valueOf(item.get("status"))))
                .collect(Collectors.toList());
    }

    private <T> BigDecimal sum(
            List<T> source,
            Function<T, BigDecimal> valueFunction
    ) {
        return source.stream()
                .map(valueFunction)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percent(long numerator, long denominator) {
        return new BigDecimal(numerator).multiply(new BigDecimal("100"))
                .divide(new BigDecimal(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
