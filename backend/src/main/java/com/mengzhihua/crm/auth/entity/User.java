package com.mengzhihua.crm.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.Role;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_user")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;
    private String displayName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private boolean enabled = true;
    private String email;
    private String phone;
}
