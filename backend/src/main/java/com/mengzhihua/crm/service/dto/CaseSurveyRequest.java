package com.mengzhihua.crm.service.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class CaseSurveyRequest {
    @Min(1)
    @Max(5)
    private Integer score;
    private String comment;
}
