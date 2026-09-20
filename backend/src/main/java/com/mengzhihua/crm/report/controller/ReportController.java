package com.mengzhihua.crm.report.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "报表中心")
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales-funnel")
    public Result<?> salesFunnel() {
        return Result.ok(reportService.salesFunnel());
    }

    @GetMapping("/sales-performance")
    public Result<?> salesPerformance(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        LocalDate now = LocalDate.now();
        return Result.ok(reportService.salesPerformance(
                year == null ? now.getYear() : year,
                month == null ? now.getMonthValue() : month
        ));
    }

    @GetMapping("/case-analysis")
    public Result<?> caseAnalysis(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        return Result.ok(reportService.caseAnalysis(start, end));
    }

    @GetMapping("/campaign-roi")
    public Result<?> campaignRoi() {
        return Result.ok(reportService.campaignRoi());
    }

    @GetMapping("/receivables")
    public Result<?> receivables() {
        return Result.ok(reportService.receivables());
    }

    @GetMapping("/sales-funnel/export")
    public ResponseEntity<byte[]> salesFunnelExport() {
        return reportService.export("sales-funnel", 0, 0);
    }

    @GetMapping("/sales-performance/export")
    public ResponseEntity<byte[]> salesPerformanceExport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        LocalDate now = LocalDate.now();
        return reportService.export(
                "sales-performance",
                year == null ? now.getYear() : year,
                month == null ? now.getMonthValue() : month
        );
    }

    @GetMapping("/case-analysis/export")
    public ResponseEntity<byte[]> caseAnalysisExport() {
        return reportService.export("case-analysis", 0, 0);
    }

    @GetMapping("/campaign-roi/export")
    public ResponseEntity<byte[]> campaignRoiExport() {
        return reportService.export("campaign-roi", 0, 0);
    }

    @GetMapping("/receivables/export")
    public ResponseEntity<byte[]> receivablesExport() {
        return reportService.export("receivables", 0, 0);
    }
}
