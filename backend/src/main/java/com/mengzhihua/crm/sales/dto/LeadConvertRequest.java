package com.mengzhihua.crm.sales.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeadConvertRequest {
    @NotNull
    private Boolean createOpportunity;
    private String opportunityName;
    private BigDecimal amount;
    private LocalDate expectedCloseDate;
    private Long accountId;
}
