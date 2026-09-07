package com.mengzhihua.crm.sales.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
public class QuoteDiscountRequest {
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal discountRate = BigDecimal.ZERO;
}
