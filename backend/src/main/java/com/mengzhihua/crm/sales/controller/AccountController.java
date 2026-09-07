package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.CsvExportService;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.service.AccountService;
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

import java.util.List;

@Tag(name = "客户")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "分页查询客户")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
    @GetMapping
    public Result<PageResult<Account>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.ok(accountService.list(page, size, keyword));
    }

    @Operation(summary = "查询客户详情")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
    @GetMapping("/{id}")
    public Result<Account> get(@PathVariable Long id) {
        return Result.ok(accountService.get(id));
    }

    @Operation(summary = "客户360视图")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
    @GetMapping("/{id}/overview")
    public Result<?> overview(@PathVariable Long id) {
        return Result.ok(accountService.overview(id));
    }

    @Operation(summary = "新增客户")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @PostMapping
    public Result<Account> add(@RequestBody Account account) {
        return Result.ok(accountService.save(account));
    }

    @Operation(summary = "编辑客户")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @PutMapping("/{id}")
    public Result<Account> edit(
            @PathVariable Long id,
            @RequestBody Account account
    ) {
        account.setId(id);
        return Result.ok(accountService.save(account));
    }

    @Operation(summary = "删除客户")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "导出客户")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword
    ) {
        List<Account> records = accountService.list(
                1,
                10000,
                keyword
        ).getRecords();
        List<List<?>> rows = records.stream()
                .map(item -> java.util.Arrays.asList(
                        item.getId(),
                        item.getName(),
                        item.getIndustry(),
                        item.getPhone(),
                        item.getOwner()
                ))
                .collect(java.util.stream.Collectors.toList());
        return CsvExportService.download(
                "accounts.csv",
                java.util.Arrays.asList("ID", "名称", "行业", "电话", "负责人"),
                rows
        );
    }
}
