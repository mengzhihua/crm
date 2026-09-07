package com.mengzhihua.crm.auth.controller;

import com.mengzhihua.crm.auth.dto.PasswordRequest;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.service.UserService;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.ok(userService.list(page, size, keyword));
    }

    @PostMapping
    public Result<User> add(@RequestBody User user) {
        return Result.ok(userService.save(user));
    }

    @PutMapping("/{id}")
    public Result<User> edit(
            @PathVariable Long id,
            @RequestBody User user
    ) {
        user.setId(id);
        return Result.ok(userService.save(user));
    }

    @PutMapping("/{id}/password")
    public Result<User> password(
            @PathVariable Long id,
            @Valid @RequestBody PasswordRequest request
    ) {
        return Result.ok(userService.resetPassword(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }
}
