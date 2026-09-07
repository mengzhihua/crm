package com.mengzhihua.crm.approval.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.Role;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_approval_request")
public class ApprovalRequest extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalTargetType targetType;
    @Column(nullable = false)
    private Long targetId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String submitter;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role approverRole;
    private String approver;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;
    @Column(length = 2000)
    private String comment;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
}
