package com.mengzhihua.crm.contract.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentPlanRequest {
    @NotNull
    private Integer seq;

    @NotNull
    private BigDecimal planAmount;

    @NotNull
    private LocalDate planDate;
}
