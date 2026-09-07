package com.mengzhihua.crm.contract.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.CsvExportService;
import com.mengzhihua.crm.common.enums.ContractStatus;
import com.mengzhihua.crm.contract.dto.ContractTerminateRequest;
import com.mengzhihua.crm.contract.dto.PaymentPlanRequest;
import com.mengzhihua.crm.contract.dto.PaymentRecordRequest;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.entity.PaymentPlan;
import com.mengzhihua.crm.contract.entity.PaymentRecord;
import com.mengzhihua.crm.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "合同与回款")
@Validated
@RestController
@RequestMapping("/api/contracts")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @Operation(summary = "分页查询合同")
    @GetMapping
    public Result<PageResult<Contract>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) Long accountId
    ) {
        return Result.ok(contractService.list(
                page, size, keyword, status, accountId
        ));
    }

    @Operation(summary = "查询合同")
    @GetMapping("/{id}")
    public Result<Contract> get(@PathVariable Long id) {
        return Result.ok(contractService.get(id));
    }

    @Operation(summary = "新增合同")
    @PostMapping
    public Result<Contract> add(@Valid @RequestBody Contract contract) {
        return Result.ok(contractService.save(contract));
    }

    @Operation(summary = "编辑合同")
    @PutMapping("/{id}")
    public Result<Contract> edit(
            @PathVariable Long id,
            @Valid @RequestBody Contract contract
    ) {
        contract.setId(id);
        return Result.ok(contractService.save(contract));
    }

    @Operation(summary = "删除合同")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "从报价生成合同")
    @PostMapping("/from-quote/{quoteId}")
    public Result<Contract> fromQuote(@PathVariable Long quoteId) {
        return Result.ok(contractService.fromQuote(quoteId));
    }

    @Operation(summary = "激活合同")
    @PutMapping("/{id}/activate")
    public Result<Contract> activate(@PathVariable Long id) {
        return Result.ok(contractService.activate(id));
    }

    @Operation(summary = "终止合同")
    @PutMapping("/{id}/terminate")
    public Result<Contract> terminate(
            @PathVariable Long id,
            @Valid @RequestBody ContractTerminateRequest request
    ) {
        return Result.ok(contractService.terminate(id, request));
    }

    @Operation(summary = "刷新过期合同")
    @PostMapping("/refresh-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> refreshExpired() {
        contractService.refreshExpired();
        return Result.ok();
    }

    @Operation(summary = "查询回款计划")
    @GetMapping("/{id}/payment-plans")
    public Result<List<PaymentPlan>> plans(@PathVariable Long id) {
        return Result.ok(contractService.plans(id));
    }

    @Operation(summary = "新增回款计划")
    @PostMapping("/{id}/payment-plans")
    public Result<PaymentPlan> addPlan(
            @PathVariable Long id,
            @Valid @RequestBody PaymentPlanRequest request
    ) {
        return Result.ok(contractService.addPlan(id, request));
    }

    @Operation(summary = "查询回款记录")
    @GetMapping("/{id}/payments")
    public Result<List<PaymentRecord>> payments(@PathVariable Long id) {
        return Result.ok(contractService.payments(id));
    }

    @Operation(summary = "登记回款")
    @PostMapping("/{id}/payments")
    public Result<PaymentRecord> addPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRecordRequest request
    ) {
        return Result.ok(contractService.addPayment(id, request));
    }

    @Operation(summary = "导出合同")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) Long accountId
    ) {
        List<Contract> records = contractService.list(
                1,
                10000,
                keyword,
                status,
                accountId
        ).getRecords();
        List<List<?>> rows = records.stream()
                .map(item -> java.util.Arrays.asList(
                        item.getContractNo(),
                        item.getName(),
                        item.getAmount(),
                        item.getStatus(),
                        item.getOwner()
                ))
                .collect(java.util.stream.Collectors.toList());
        return CsvExportService.download(
                "contracts.csv",
                java.util.Arrays.asList("合同号", "名称", "金额", "状态", "负责人"),
                rows
        );
    }
}
