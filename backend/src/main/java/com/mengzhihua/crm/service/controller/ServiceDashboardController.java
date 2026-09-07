package com.mengzhihua.crm.service.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.service.entity.CaseSurvey;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CaseSurveyRepository;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "服务指标")
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
public class ServiceDashboardController {
    private final CrmCaseRepository caseRepository;
    private final CaseSurveyRepository surveyRepository;

    public ServiceDashboardController(
            CrmCaseRepository caseRepository,
            CaseSurveyRepository surveyRepository
    ) {
        this.caseRepository = caseRepository;
        this.surveyRepository = surveyRepository;
    }

    @Operation(summary = "查询服务指标")
    @GetMapping("/service")
    public Result<Map<String, Object>> service() {
        List<CaseSurvey> surveys = surveyRepository.findAllByScoreIsNotNull();
        BigDecimal averageScore = surveys.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(surveys.stream()
                .mapToInt(CaseSurvey::getScore)
                .average()
                .orElse(0));
        List<CrmCase> cases = caseRepository.findAll();
        long responseCount = cases.stream()
                .filter(item -> item.getFirstResponseAt() != null)
                .count();
        BigDecimal responseHours = averageHours(cases, true);
        BigDecimal resolveHours = averageHours(cases, false);
        long slaTotal = cases.stream()
                .filter(item -> item.getSlaDueAt() != null
                        && item.getResolvedAt() != null)
                .count();
        long slaMet = cases.stream()
                .filter(item -> item.getSlaDueAt() != null
                        && item.getResolvedAt() != null
                        && !item.getResolvedAt().isAfter(item.getSlaDueAt()))
                .count();
        Map<String, Long> ownerCounts = new LinkedHashMap<>();
        for (CrmCase item : cases) {
            ownerCounts.put(
                    item.getOwner(),
                    ownerCounts.getOrDefault(item.getOwner(), 0L) + 1
            );
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("averageSatisfactionScore", averageScore);
        result.put("averageFirstResponseHours", responseHours);
        result.put("averageResolveHours", resolveHours);
        result.put(
                "slaAchievementRate",
                slaTotal == 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(slaMet)
                        .divide(BigDecimal.valueOf(slaTotal), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
        );
        result.put("ownerCaseCount", ownerCounts);
        result.put("responseCount", responseCount);
        return Result.ok(result);
    }

    private BigDecimal averageHours(List<CrmCase> cases, boolean response) {
        List<Long> values = cases.stream()
                .filter(item -> item.getCreatedAt() != null)
                .map(item -> {
                    LocalDateTime end = response
                            ? item.getFirstResponseAt()
                            : item.getResolvedAt();
                    return end == null
                            ? null
                            : Duration.between(item.getCreatedAt(), end).toHours();
                })
                .filter(item -> item != null)
                .collect(java.util.stream.Collectors.toList());
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(values.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0));
    }
}
