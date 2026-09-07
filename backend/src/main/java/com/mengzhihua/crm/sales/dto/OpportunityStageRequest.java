package com.mengzhihua.crm.sales.dto;

import com.mengzhihua.crm.common.enums.OpportunityStage;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OpportunityStageRequest {
    @NotNull
    private OpportunityStage stage;
    private Integer probability;
    private String lostReason;
}
