package com.mengzhihua.crm.service.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.ArticleStatus;
import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import com.mengzhihua.crm.service.service.KnowledgeService;
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
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "知识库")
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "搜索知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @GetMapping
    public Result<PageResult<KnowledgeArticle>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ArticleStatus status
    ) {
        return Result.ok(knowledgeService.list(page, size, keyword, status));
    }

    @Operation(summary = "查看知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT','SALES_MANAGER','SALES_REP')")
    @GetMapping("/{id}")
    public Result<KnowledgeArticle> get(@PathVariable Long id) {
        return Result.ok(knowledgeService.get(id));
    }

    @Operation(summary = "新增知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PostMapping
    public Result<KnowledgeArticle> add(@RequestBody KnowledgeArticle article) {
        return Result.ok(knowledgeService.save(article));
    }

    @Operation(summary = "编辑知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}")
    public Result<KnowledgeArticle> edit(
            @PathVariable Long id,
            @RequestBody KnowledgeArticle article
    ) {
        article.setId(id);
        return Result.ok(knowledgeService.save(article));
    }

    @Operation(summary = "发布知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @PutMapping("/{id}/publish")
    public Result<KnowledgeArticle> publish(@PathVariable Long id) {
        return Result.ok(knowledgeService.publish(id));
    }

    @Operation(summary = "删除知识文章")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_AGENT')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.ok();
    }
}
