package com.mengzhihua.crm.approval.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.Role;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_approval_step")
public class ApprovalStep extends BaseEntity {
    private Long requestId;
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role approverRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    private String approver;

    @Column(length = 2000)
    private String comment;

    private LocalDateTime decidedAt;

    @Version
    private Long version;
}
