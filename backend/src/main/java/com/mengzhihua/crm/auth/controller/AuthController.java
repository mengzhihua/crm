package com.mengzhihua.crm.auth.controller;

import com.mengzhihua.crm.auth.dto.LoginRequest;
import com.mengzhihua.crm.auth.dto.LoginResponse;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.service.JwtTokenService;
import com.mengzhihua.crm.auth.service.UserService;
import com.mengzhihua.crm.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtTokenService tokenService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user;
        try {
            user = userService.findByUsername(request.getUsername());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail("用户名或密码错误"));
        }
        if (!user.isEnabled()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail("用户名或密码错误"));
        }
        return ResponseEntity.ok(Result.ok(new LoginResponse(
                tokenService.create(user),
                user
        )));
    }

    @Operation(summary = "查询当前用户")
    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(userService.findByUsername(
                com.mengzhihua.crm.auth.CurrentUser.username()
        ));
    }
}
