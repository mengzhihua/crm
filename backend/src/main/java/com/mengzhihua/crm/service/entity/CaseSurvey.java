package com.mengzhihua.crm.service.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_case_survey")
public class CaseSurvey extends BaseEntity {
    private Long caseId;
    private Integer score;

    @Column(length = 2000)
    private String comment;
}
