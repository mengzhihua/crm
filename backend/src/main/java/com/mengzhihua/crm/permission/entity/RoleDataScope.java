package com.mengzhihua.crm.permission.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.common.enums.DataScope;
import com.mengzhihua.crm.common.enums.Role;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_role_data_scope")
public class RoleDataScope extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataObjectType objectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataScope scope;
}
