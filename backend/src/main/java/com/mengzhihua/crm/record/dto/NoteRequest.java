package com.mengzhihua.crm.record.dto;

import com.mengzhihua.crm.common.enums.RelatedType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NoteRequest {
    @NotNull
    private RelatedType targetType;
    @NotNull
    private Long targetId;
    @NotBlank
    private String content;
}
