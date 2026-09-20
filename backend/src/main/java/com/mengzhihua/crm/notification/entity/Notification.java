package com.mengzhihua.crm.notification.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.NotificationType;
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
@Table(name = "crm_notification")
public class Notification extends BaseEntity {
    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private RelatedType relatedType;

    private Long relatedId;
    private boolean read;
    private LocalDateTime readAt;
}
