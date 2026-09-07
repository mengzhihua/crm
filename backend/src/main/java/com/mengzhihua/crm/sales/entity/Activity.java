package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ActivityStatus;
import com.mengzhihua.crm.common.enums.ActivityType;
import com.mengzhihua.crm.common.enums.RelatedType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_activity")
public class Activity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ActivityType type;
    private String subject;

    @Column(length = 3000)
    private String content;

    @Enumerated(EnumType.STRING)
    private RelatedType relatedType;
    private Long relatedId;
    private LocalDateTime dueTime;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;
    private LocalDateTime completedAt;
}
