package com.mengzhihua.crm.contract.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.PaymentStatus;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "crm_payment_plan")
public class PaymentPlan extends BaseEntity {
    private Long contractId;
    private Integer seq;
    private BigDecimal planAmount = BigDecimal.ZERO;
    private LocalDate planDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.UNPAID;

    private BigDecimal paidAmount = BigDecimal.ZERO;
}
