package com.mengzhihua.crm.approval.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.Role;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role approverRole;
    @Column(nullable = false)
    private int priority;
    @Column(nullable = false)
    private boolean active = true;
}
