package com.mengzhihua.crm.approval.controller;

import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "审批规则")
@RestController
@RequestMapping("/api/approval-rules")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
public class ApprovalRuleController {
    private final ApprovalRuleRepository ruleRepository;

    public ApprovalRuleController(ApprovalRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public Result<List<ApprovalRule>> list() {
        return Result.ok(ruleRepository.findAll());
    }

    @PostMapping
    public Result<ApprovalRule> add(@RequestBody ApprovalRule rule) {
        return Result.ok(ruleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public Result<ApprovalRule> edit(
            @PathVariable Long id,
            @RequestBody ApprovalRule rule
    ) {
        rule.setId(id);
        return Result.ok(ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        return Result.ok();
    }
}
