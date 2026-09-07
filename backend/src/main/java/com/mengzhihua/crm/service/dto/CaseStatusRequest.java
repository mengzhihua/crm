package com.mengzhihua.crm.service.dto;

import com.mengzhihua.crm.common.enums.CaseStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CaseStatusRequest {
    @NotNull
    private CaseStatus status;
    private String solution;
}
