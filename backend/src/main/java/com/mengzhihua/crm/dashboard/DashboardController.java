package com.mengzhihua.crm.dashboard;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.ActivityRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.OpportunityService;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final CrmCaseRepository caseRepository;
    private final OpportunityService opportunityService;
    private final ActivityRepository activityRepository;

    public DashboardController(
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            CrmCaseRepository caseRepository,
            OpportunityService opportunityService,
            ActivityRepository activityRepository
    ) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseRepository = caseRepository;
        this.opportunityService = opportunityService;
        this.activityRepository = activityRepository;
    }

    @Operation(summary = "查询仪表盘汇总")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        List<OpportunityStage> closedStages = Arrays.asList(
                OpportunityStage.CLOSED_WON,
                OpportunityStage.CLOSED_LOST
        );
        List<CaseStatus> closedCases = Arrays.asList(
                CaseStatus.RESOLVED,
                CaseStatus.CLOSED
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("newLeadCount", leadRepository.countByStatus(LeadStatus.NEW));
        result.put(
                "openOpportunityCount",
                opportunityRepository.countByStageNotIn(closedStages)
        );
        result.put("openOpportunityAmount", openOpportunityAmount(closedStages));
        result.put("wonAmountThisMonth", wonAmountThisMonth());
        result.put("openCaseCount", caseRepository.countByStatusNotIn(closedCases));
        result.put(
                "overdueCaseCount",
                caseRepository.countBySlaDueAtBeforeAndStatusNotIn(
                        LocalDateTime.now(),
                        closedCases
                )
        );
        result.put("pipeline", opportunityService.pipeline());
        result.put("leadBySource", leadBySource());
        result.put("caseByStatus", caseByStatus());
        result.put("caseByPriority", caseByPriority());
        result.put(
                "recentActivities",
                activityRepository.findTop10ByOrderByDueTimeDesc()
        );
        return Result.ok(result);
    }

    private BigDecimal openOpportunityAmount(
            List<OpportunityStage> closedStages
    ) {
        return opportunityRepository.findByStageNotIn(closedStages).stream()
                .map(Opportunity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal wonAmountThisMonth() {
        return opportunityRepository.findByStage(OpportunityStage.CLOSED_WON)
                .stream()
                .filter(this::closedThisMonth)
                .map(Opportunity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean closedThisMonth(Opportunity opportunity) {
        return opportunity.getClosedAt() != null
                && opportunity.getClosedAt().getMonth() == LocalDate.now().getMonth()
                && opportunity.getClosedAt().getYear() == LocalDate.now().getYear();
    }

    private Map<String, Long> leadBySource() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Lead lead : leadRepository.findAll()) {
            String key = String.valueOf(lead.getSource());
            result.put(key, result.getOrDefault(key, 0L) + 1);
        }
        return result;
    }

    private Map<String, Long> caseByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (CrmCase crmCase : caseRepository.findAll()) {
            String key = String.valueOf(crmCase.getStatus());
            result.put(key, result.getOrDefault(key, 0L) + 1);
        }
        return result;
    }

    private Map<String, Long> caseByPriority() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (CrmCase crmCase : caseRepository.findAll()) {
            String key = String.valueOf(crmCase.getPriority());
            result.put(key, result.getOrDefault(key, 0L) + 1);
        }
        return result;
    }
}
