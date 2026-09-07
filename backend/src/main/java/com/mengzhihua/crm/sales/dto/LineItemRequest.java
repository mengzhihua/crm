package com.mengzhihua.crm.sales.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class LineItemRequest {
    @NotNull
    private Long productId;
    private Long priceBookEntryId;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal quantity;
    @NotNull
    @DecimalMin("0")
    private BigDecimal unitPrice;
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal discountRate = BigDecimal.ZERO;
}
