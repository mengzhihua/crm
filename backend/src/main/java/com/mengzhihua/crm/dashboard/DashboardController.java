package com.mengzhihua.crm.dashboard;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.ActivityRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.OpportunityService;
import com.mengzhihua.crm.forecast.service.ForecastService;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
public class DashboardController {
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final CrmCaseRepository caseRepository;
    private final OpportunityService opportunityService;
    private final ActivityRepository activityRepository;
    private final ForecastService forecastService;
    private final DashboardLayoutRepository layoutRepository;

    public DashboardController(
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            CrmCaseRepository caseRepository,
            OpportunityService opportunityService,
            ActivityRepository activityRepository,
            ForecastService forecastService,
            DashboardLayoutRepository layoutRepository
    ) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseRepository = caseRepository;
        this.opportunityService = opportunityService;
        this.activityRepository = activityRepository;
        this.forecastService = forecastService;
        this.layoutRepository = layoutRepository;
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
        Map<String, Object> forecast = forecastService.currentSummary();
        result.put("targetAmount", forecast.get("targetAmount"));
        result.put("achievementRate", forecast.get("achievementRate"));
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

    @GetMapping("/widgets")
    public Result<List<Map<String, Object>>> widgets() {
        List<Map<String, Object>> result = new ArrayList<>();
        addWidget(result, "newLeadCount", "新增线索", "metric", 6);
        addWidget(result, "openOpportunityCount", "进行中商机", "metric", 6);
        addWidget(result, "wonAmountThisMonth", "本月赢单金额", "metric", 6);
        addWidget(result, "overdueCaseCount", "超期工单", "metric", 6);
        addWidget(result, "pipeline", "商机管道", "chart", 12);
        addWidget(result, "leadBySource", "线索来源", "chart", 12);
        addWidget(result, "caseByStatus", "工单状态", "chart", 12);
        addWidget(result, "caseByPriority", "工单优先级", "chart", 12);
        addWidget(result, "myPendingApprovals", "待我审批", "list", 6);
        addWidget(result, "myOpenCases", "我的工单", "list", 6);
        addWidget(result, "unreadNotifications", "未读通知", "metric", 6);
        addWidget(result, "recentActivities", "最近活动", "list", 12);
        return Result.ok(result);
    }

    @GetMapping("/layout")
    public Result<DashboardLayout> layout() {
        String username = CurrentUser.usernameOrDefault();
        DashboardLayout layout = layoutRepository.findByUsername(username)
                .orElseGet(() -> {
                    DashboardLayout value = new DashboardLayout();
                    value.setUsername(username);
                    value.setWidgetsJson(defaultWidgets());
                    return value;
                });
        return Result.ok(layout);
    }

    @PutMapping("/layout")
    public Result<DashboardLayout> saveLayout(@RequestBody DashboardLayout layout) {
        String username = CurrentUser.usernameOrDefault();
        DashboardLayout saved = layoutRepository.findByUsername(username)
                .orElseGet(DashboardLayout::new);
        saved.setUsername(username);
        saved.setWidgetsJson(layout.getWidgetsJson());
        return Result.ok(layoutRepository.save(saved));
    }

    @DeleteMapping("/layout")
    public Result<Void> resetLayout() {
        layoutRepository.findByUsername(CurrentUser.usernameOrDefault())
                .ifPresent(layoutRepository::delete);
        return Result.ok();
    }

    @GetMapping("/widget/{key}")
    public Result<Object> widget(@PathVariable String key) {
        Map<String, Object> data = summary().getData();
        if ("unreadNotifications".equals(key)) {
            data = new LinkedHashMap<>();
            data.put("count", 0);
        } else if ("myPendingApprovals".equals(key)) {
            data = new LinkedHashMap<>();
            data.put("records", new ArrayList<>());
        } else if ("myOpenCases".equals(key)) {
            data = new LinkedHashMap<>();
            data.put("records", new ArrayList<>());
        }
        return Result.ok(data.get(key) == null ? data : data.get(key));
    }

    private void addWidget(
            List<Map<String, Object>> widgets,
            String key,
            String title,
            String type,
            int defaultSpan
    ) {
        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("key", key);
        widget.put("title", title);
        widget.put("type", type);
        widget.put("defaultSpan", defaultSpan);
        widgets.add(widget);
    }

    private String defaultWidgets() {
        return "[{\"key\":\"newLeadCount\",\"span\":6},"
                + "{\"key\":\"openOpportunityCount\",\"span\":6},"
                + "{\"key\":\"wonAmountThisMonth\",\"span\":6},"
                + "{\"key\":\"overdueCaseCount\",\"span\":6},"
                + "{\"key\":\"pipeline\",\"span\":12},"
                + "{\"key\":\"leadBySource\",\"span\":12},"
                + "{\"key\":\"caseByStatus\",\"span\":12},"
                + "{\"key\":\"caseByPriority\",\"span\":12},"
                + "{\"key\":\"recentActivities\",\"span\":12}]";
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
