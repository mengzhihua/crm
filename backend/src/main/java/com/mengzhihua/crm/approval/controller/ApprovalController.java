package com.mengzhihua.crm.approval.controller;

import com.mengzhihua.crm.approval.dto.ApprovalDecision;
import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.service.ApprovalService;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "审批申请")
@RestController
@RequestMapping("/api/approvals")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public Result<PageResult<ApprovalRequest>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "false") boolean mine
    ) {
        return Result.ok(approvalService.list(page, size, status, mine));
    }

    @PutMapping("/{id}/approve")
    public Result<ApprovalRequest> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecision decision
    ) {
        return Result.ok(approvalService.decide(id, true, decision));
    }

    @PutMapping("/{id}/reject")
    public Result<ApprovalRequest> reject(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecision decision
    ) {
        return Result.ok(approvalService.decide(id, false, decision));
    }
}
