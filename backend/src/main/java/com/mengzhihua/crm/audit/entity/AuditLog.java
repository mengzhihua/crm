package com.mengzhihua.crm.audit.entity;

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
@Table(name = "crm_audit_log")
public class AuditLog extends BaseEntity {
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String action;

    private String module;
    private String method;

    @Column(length = 500)
    private String path;

    @Column(length = 500)
    private String query;

    private int status;
    private long durationMs;
    private String ip;
}
