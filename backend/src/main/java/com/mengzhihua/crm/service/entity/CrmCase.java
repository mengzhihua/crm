package com.mengzhihua.crm.service.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.CaseOrigin;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "crm_case",
        uniqueConstraints = @UniqueConstraint(columnNames = "caseNo")
)
public class CrmCase extends BaseEntity {
    @Column(nullable = false)
    private String caseNo;
    private Long accountId;
    private Long contactId;
    private String subject;

    @Column(length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    private CaseType type;

    @Enumerated(EnumType.STRING)
    private CasePriority priority;

    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    @Enumerated(EnumType.STRING)
    private CaseOrigin origin;

    private LocalDateTime slaDueAt;
    private Boolean escalated;

    @Column(length = 3000)
    private String solution;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
}
