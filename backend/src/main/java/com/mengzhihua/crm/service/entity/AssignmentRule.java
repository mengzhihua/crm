package com.mengzhihua.crm.service.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.CaseOrigin;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_assignment_rule")
public class AssignmentRule extends BaseEntity {
    private String name;
    private Integer priority;
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private CasePriority casePriority;

    @Enumerated(EnumType.STRING)
    private CaseOrigin origin;

    @Column(length = 500)
    private String keyword;

    private String assignTo;
}
