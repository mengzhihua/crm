package com.mengzhihua.crm.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CaseAssignRequest {
    @NotBlank
    private String owner;
}
