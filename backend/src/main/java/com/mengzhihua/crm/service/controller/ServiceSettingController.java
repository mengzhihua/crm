package com.mengzhihua.crm.service.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.service.entity.AssignmentRule;
import com.mengzhihua.crm.service.entity.SlaPolicy;
import com.mengzhihua.crm.service.repository.AssignmentRuleRepository;
import com.mengzhihua.crm.service.repository.SlaPolicyRepository;
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
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "服务设置")
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceSettingController {
    private final SlaPolicyRepository slaPolicyRepository;
    private final AssignmentRuleRepository assignmentRuleRepository;

    public ServiceSettingController(
            SlaPolicyRepository slaPolicyRepository,
            AssignmentRuleRepository assignmentRuleRepository
    ) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.assignmentRuleRepository = assignmentRuleRepository;
    }

    @Operation(summary = "查询 SLA 策略")
    @GetMapping("/sla-policies")
    public Result<List<SlaPolicy>> slaPolicies() {
        return Result.ok(slaPolicyRepository.findAll());
    }

    @Operation(summary = "新增 SLA 策略")
    @PostMapping("/sla-policies")
    public Result<SlaPolicy> addSla(@Valid @RequestBody SlaPolicy policy) {
        return Result.ok(slaPolicyRepository.save(policy));
    }

    @Operation(summary = "修改 SLA 策略")
    @PutMapping("/sla-policies/{id}")
    public Result<SlaPolicy> editSla(
            @PathVariable Long id,
            @Valid @RequestBody SlaPolicy policy
    ) {
        policy.setId(id);
        return Result.ok(slaPolicyRepository.save(policy));
    }

    @Operation(summary = "删除 SLA 策略")
    @DeleteMapping("/sla-policies/{id}")
    public Result<Void> deleteSla(@PathVariable Long id) {
        slaPolicyRepository.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "查询分派规则")
    @GetMapping("/assignment-rules")
    public Result<List<AssignmentRule>> assignmentRules() {
        return Result.ok(
                assignmentRuleRepository.findByActiveTrueOrderByPriorityAsc()
        );
    }

    @Operation(summary = "新增分派规则")
    @PostMapping("/assignment-rules")
    public Result<AssignmentRule> addAssignment(
            @Valid @RequestBody AssignmentRule rule
    ) {
        return Result.ok(assignmentRuleRepository.save(rule));
    }

    @Operation(summary = "修改分派规则")
    @PutMapping("/assignment-rules/{id}")
    public Result<AssignmentRule> editAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRule rule
    ) {
        rule.setId(id);
        return Result.ok(assignmentRuleRepository.save(rule));
    }

    @Operation(summary = "删除分派规则")
    @DeleteMapping("/assignment-rules/{id}")
    public Result<Void> deleteAssignment(@PathVariable Long id) {
        assignmentRuleRepository.deleteById(id);
        return Result.ok();
    }
}
