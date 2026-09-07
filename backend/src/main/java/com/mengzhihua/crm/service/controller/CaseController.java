package com.mengzhihua.crm.service.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.service.dto.CaseAssignRequest;
import com.mengzhihua.crm.service.dto.CaseCommentRequest;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.entity.CaseComment;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.service.CaseService;
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

@Tag(name = "服务工单")
@RestController
@RequestMapping("/api/cases")
public class CaseController {
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @Operation(summary = "分页查询工单")
    @GetMapping
    public Result<PageResult<CrmCase>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CasePriority priority,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Boolean overdue
    ) {
        return Result.ok(caseService.list(
                page, size, keyword, status, priority, accountId, overdue
        ));
    }

    @Operation(summary = "查询工单")
    @GetMapping("/{id}")
    public Result<CrmCase> get(@PathVariable Long id) {
        return Result.ok(caseService.get(id));
    }

    @Operation(summary = "新增工单")
    @PostMapping
    public Result<CrmCase> add(@RequestBody CrmCase crmCase) {
        return Result.ok(caseService.save(crmCase));
    }

    @Operation(summary = "编辑工单")
    @PutMapping("/{id}")
    public Result<CrmCase> edit(
            @PathVariable Long id,
            @RequestBody CrmCase crmCase
    ) {
        crmCase.setId(id);
        return Result.ok(caseService.save(crmCase));
    }

    @Operation(summary = "删除工单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        caseService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "变更工单状态")
    @PutMapping("/{id}/status")
    public Result<CrmCase> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody CaseStatusRequest request
    ) {
        return Result.ok(caseService.changeStatus(id, request));
    }

    @Operation(summary = "升级工单")
    @PutMapping("/{id}/escalate")
    public Result<CrmCase> escalate(@PathVariable Long id) {
        return Result.ok(caseService.escalate(id));
    }

    @Operation(summary = "分派工单")
    @PutMapping("/{id}/assign")
    public Result<CrmCase> assign(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssignRequest request
    ) {
        return Result.ok(caseService.assign(id, request));
    }

    @Operation(summary = "查询工单评论")
    @GetMapping("/{id}/comments")
    public Result<List<CaseComment>> comments(@PathVariable Long id) {
        return Result.ok(caseService.comments(id));
    }

    @Operation(summary = "新增工单评论")
    @PostMapping("/{id}/comments")
    public Result<CaseComment> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CaseCommentRequest request
    ) {
        return Result.ok(caseService.addComment(id, request));
    }
}
