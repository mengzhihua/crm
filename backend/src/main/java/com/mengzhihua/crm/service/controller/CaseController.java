package com.mengzhihua.crm.service.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.CsvExportService;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.service.dto.CaseAssignRequest;
import com.mengzhihua.crm.service.dto.CaseCommentRequest;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.dto.CaseSurveyRequest;
import com.mengzhihua.crm.service.entity.CaseArticleLink;
import com.mengzhihua.crm.service.entity.CaseComment;
import com.mengzhihua.crm.service.entity.CaseSurvey;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
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
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @GetMapping("/{id}")
    public Result<CrmCase> get(@PathVariable Long id) {
        return Result.ok(caseService.get(id));
    }

    @Operation(summary = "新增工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PostMapping
    public Result<CrmCase> add(@RequestBody CrmCase crmCase) {
        return Result.ok(caseService.save(crmCase));
    }

    @Operation(summary = "编辑工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}")
    public Result<CrmCase> edit(
            @PathVariable Long id,
            @RequestBody CrmCase crmCase
    ) {
        crmCase.setId(id);
        return Result.ok(caseService.save(crmCase));
    }

    @Operation(summary = "删除工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        caseService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "变更工单状态")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}/status")
    public Result<CrmCase> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody CaseStatusRequest request
    ) {
        return Result.ok(caseService.changeStatus(id, request));
    }

    @Operation(summary = "升级工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}/escalate")
    public Result<CrmCase> escalate(@PathVariable Long id) {
        return Result.ok(caseService.escalate(id));
    }

    @Operation(summary = "分派工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}/assign")
    public Result<CrmCase> assign(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssignRequest request
    ) {
        return Result.ok(caseService.assign(id, request));
    }

    @Operation(summary = "查询工单评论")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @GetMapping("/{id}/comments")
    public Result<List<CaseComment>> comments(@PathVariable Long id) {
        return Result.ok(caseService.comments(id));
    }

    @Operation(summary = "新增工单评论")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PostMapping("/{id}/comments")
    public Result<CaseComment> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CaseCommentRequest request
    ) {
        return Result.ok(caseService.addComment(id, request));
    }

    @Operation(summary = "评价工单")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @PostMapping("/{id}/survey")
    public Result<CaseSurvey> survey(
            @PathVariable Long id,
            @Valid @RequestBody CaseSurveyRequest request
    ) {
        return Result.ok(caseService.survey(id, request));
    }

    @Operation(summary = "查询关联知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @GetMapping("/{id}/articles")
    public Result<List<CaseArticleLink>> articles(@PathVariable Long id) {
        return Result.ok(caseService.articles(id));
    }

    @Operation(summary = "关联知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PostMapping("/{id}/articles/{articleId}")
    public Result<CaseArticleLink> addArticle(
            @PathVariable Long id,
            @PathVariable Long articleId
    ) {
        return Result.ok(caseService.addArticle(id, articleId));
    }

    @Operation(summary = "取消关联知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @DeleteMapping("/{id}/articles/{articleId}")
    public Result<Void> removeArticle(
            @PathVariable Long id,
            @PathVariable Long articleId
    ) {
        caseService.removeArticle(id, articleId);
        return Result.ok();
    }

    @Operation(summary = "检查工单 SLA")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/check-sla")
    public Result<Void> checkSla() {
        caseService.checkSla();
        return Result.ok();
    }

    @Operation(summary = "导出工单")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CasePriority priority,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Boolean overdue
    ) {
        List<CrmCase> records = caseService.list(
                1,
                10000,
                keyword,
                status,
                priority,
                accountId,
                overdue
        ).getRecords();
        List<List<?>> rows = records.stream()
                .map(item -> java.util.Arrays.asList(
                        item.getCaseNo(),
                        item.getSubject(),
                        item.getStatus(),
                        item.getPriority(),
                        item.getOwner()
                ))
                .collect(java.util.stream.Collectors.toList());
        return CsvExportService.download(
                "cases.csv",
                java.util.Arrays.asList("工单号", "主题", "状态", "优先级", "负责人"),
                rows
        );
    }
}
