package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
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

@Tag(name = "客户")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "分页查询客户")
    @GetMapping
    public Result<PageResult<Account>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.ok(accountService.list(page, size, keyword));
    }

    @Operation(summary = "查询客户详情")
    @GetMapping("/{id}")
    public Result<Account> get(@PathVariable Long id) {
        return Result.ok(accountService.get(id));
    }

    @Operation(summary = "客户360视图")
    @GetMapping("/{id}/overview")
    public Result<?> overview(@PathVariable Long id) {
        return Result.ok(accountService.overview(id));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    public Result<Account> add(@RequestBody Account account) {
        return Result.ok(accountService.save(account));
    }

    @Operation(summary = "编辑客户")
    @PutMapping("/{id}")
    public Result<Account> edit(
            @PathVariable Long id,
            @RequestBody Account account
    ) {
        account.setId(id);
        return Result.ok(accountService.save(account));
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return Result.ok();
    }
}
