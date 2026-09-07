package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.dto.OpportunityStageRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "商机")
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @Operation(summary = "分页查询商机")
    @GetMapping
    public Result<PageResult<Opportunity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) OpportunityStage stage
    ) {
        return Result.ok(
                opportunityService.list(page, size, keyword, accountId, stage)
        );
    }

    @Operation(summary = "查询销售管道")
    @GetMapping("/pipeline")
    public Result<List<Map<String, Object>>> pipeline() {
        return Result.ok(opportunityService.pipeline());
    }

    @Operation(summary = "查询商机")
    @GetMapping("/{id}")
    public Result<Opportunity> get(@PathVariable Long id) {
        return Result.ok(opportunityService.get(id));
    }

    @Operation(summary = "新增商机")
    @PostMapping
    public Result<Opportunity> add(@RequestBody Opportunity opportunity) {
        return Result.ok(opportunityService.save(opportunity));
    }

    @Operation(summary = "编辑商机")
    @PutMapping("/{id}")
    public Result<Opportunity> edit(
            @PathVariable Long id,
            @RequestBody Opportunity opportunity
    ) {
        opportunity.setId(id);
        return Result.ok(opportunityService.save(opportunity));
    }

    @Operation(summary = "推进商机阶段")
    @PutMapping("/{id}/stage")
    public Result<Opportunity> changeStage(
            @PathVariable Long id,
            @Valid @RequestBody OpportunityStageRequest request
    ) {
        return Result.ok(opportunityService.changeStage(id, request));
    }

    @Operation(summary = "删除商机")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return Result.ok();
    }
}
