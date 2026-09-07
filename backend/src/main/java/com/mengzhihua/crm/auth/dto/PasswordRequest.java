package com.mengzhihua.crm.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordRequest {
    @NotBlank
    private String password;
}
