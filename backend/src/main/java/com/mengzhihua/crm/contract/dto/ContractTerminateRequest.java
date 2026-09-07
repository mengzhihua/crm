package com.mengzhihua.crm.contract.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ContractTerminateRequest {
    @NotBlank
    private String reason;
}
