package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.AccountLevel;
import com.mengzhihua.crm.common.enums.AccountType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(
        name = "crm_account",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Account extends BaseEntity {
    @Column(nullable = false)
    private String name;
    private String industry;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    private AccountLevel level;

    private String phone;
    private String website;
    private String address;
    private BigDecimal annualRevenue;
    private Integer employees;

    @Column(length = 2000)
    private String description;
}
