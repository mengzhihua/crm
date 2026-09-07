package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.sales.dto.LeadConvertRequest;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
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
import java.util.Map;

@Tag(name = "线索")
@Validated
@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @Operation(summary = "分页查询线索")
    @GetMapping
    public Result<PageResult<Lead>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadSource source
    ) {
        return Result.ok(leadService.list(page, size, keyword, status, source));
    }

    @Operation(summary = "查询线索详情")
    @GetMapping("/{id}")
    public Result<Lead> get(@PathVariable Long id) {
        return Result.ok(leadService.get(id));
    }

    @Operation(summary = "新增线索")
    @PostMapping
    public Result<Lead> add(@Valid @RequestBody Lead lead) {
        return Result.ok(leadService.save(lead));
    }

    @Operation(summary = "编辑线索")
    @PutMapping("/{id}")
    public Result<Lead> edit(@PathVariable Long id, @Valid @RequestBody Lead lead) {
        lead.setId(id);
        return Result.ok(leadService.save(lead));
    }

    @Operation(summary = "删除线索")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "转化线索")
    @PostMapping("/{id}/convert")
    public Result<Map<String, Long>> convert(
            @PathVariable Long id,
            @Valid @RequestBody LeadConvertRequest request
    ) {
        return Result.ok(leadService.convert(id, request));
    }
}
