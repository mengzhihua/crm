package com.mengzhihua.crm.forecast.controller;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.forecast.entity.SalesTarget;
import com.mengzhihua.crm.forecast.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "销售预测")
@RestController
@RequestMapping("/api")
public class ForecastController {
    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @Operation(summary = "查询销售目标")
    @GetMapping("/sales-targets")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<PageResult<SalesTarget>> targets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String owner
    ) {
        String actualOwner = owner;
        if (CurrentUser.role() != null
                && CurrentUser.role().name().equals("SALES_REP")) {
            actualOwner = CurrentUser.usernameOrDefault();
        }
        return Result.ok(forecastService.targets(
                page, size, year, month, actualOwner
        ));
    }

    @Operation(summary = "新增销售目标")
    @PostMapping("/sales-targets")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<SalesTarget> addTarget(
            @Valid @RequestBody SalesTarget target
    ) {
        return Result.ok(forecastService.save(target));
    }

    @Operation(summary = "编辑销售目标")
    @PutMapping("/sales-targets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<SalesTarget> editTarget(
            @PathVariable Long id,
            @Valid @RequestBody SalesTarget target
    ) {
        target.setId(id);
        return Result.ok(forecastService.save(target));
    }

    @Operation(summary = "删除销售目标")
    @DeleteMapping("/sales-targets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> deleteTarget(@PathVariable Long id) {
        forecastService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查询销售预测")
    @GetMapping("/forecast")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<List<Map<String, Object>>> forecast(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String owner
    ) {
        String actualOwner = owner;
        if (CurrentUser.role() != null
                && CurrentUser.role().name().equals("SALES_REP")) {
            actualOwner = CurrentUser.usernameOrDefault();
        }
        return Result.ok(forecastService.forecast(year, month, actualOwner));
    }

    @Operation(summary = "查询年度预测趋势")
    @GetMapping("/forecast/trend")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<List<Map<String, Object>>> trend(@RequestParam int year) {
        return Result.ok(forecastService.trend(year));
    }
}
