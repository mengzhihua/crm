package com.mengzhihua.crm.contract.dto;

import com.mengzhihua.crm.common.enums.PaymentMethod;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRecordRequest {
    private Long planId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate paidDate;

    @NotNull
    private PaymentMethod method;

    private String voucherNo;
    private String remark;
}
