package com.mengzhihua.crm.approval.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.mengzhihua.crm.common.enums.Role;

@Data
@Entity
@Table(name = "crm_approval_rule")
public class ApprovalRule extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalTargetType targetType = ApprovalTargetType.QUOTE;
    private BigDecimal minAmount;
    private BigDecimal minDiscountRate;
    private String approverRoles;
    @Column(nullable = false)
    private int priority;
    @Column(nullable = false)
    private boolean active = true;

    public List<Role> roles() {
        if (approverRoles == null || approverRoles.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(approverRoles.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .map(Role::valueOf)
                .collect(Collectors.toList());
    }
}
