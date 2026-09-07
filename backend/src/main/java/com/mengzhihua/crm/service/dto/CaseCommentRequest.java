package com.mengzhihua.crm.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CaseCommentRequest {
    @NotBlank
    private String author;
    @NotBlank
    private String content;
    private Boolean internal = false;
}
