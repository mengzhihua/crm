package com.mengzhihua.crm.contract.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.PaymentMethod;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "crm_payment_record")
public class PaymentRecord extends BaseEntity {
    private Long contractId;
    private Long planId;
    private BigDecimal amount;
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String voucherNo;

    @Column(length = 1000)
    private String remark;
}
