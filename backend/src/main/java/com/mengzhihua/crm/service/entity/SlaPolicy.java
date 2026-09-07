package com.mengzhihua.crm.service.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.CasePriority;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@Table(
        name = "crm_sla_policy",
        uniqueConstraints = @UniqueConstraint(columnNames = "priority")
)
public class SlaPolicy extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private CasePriority priority;
    private Integer responseHours;
    private Integer resolveHours;
    private Boolean active = true;
}
