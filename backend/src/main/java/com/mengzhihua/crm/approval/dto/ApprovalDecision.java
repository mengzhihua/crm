package com.mengzhihua.crm.approval.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ApprovalDecision {
    @Size(max = 2000)
    private String comment;
}
