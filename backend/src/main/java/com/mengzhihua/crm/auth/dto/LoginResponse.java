package com.mengzhihua.crm.auth.dto;

import com.mengzhihua.crm.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User user;
}
