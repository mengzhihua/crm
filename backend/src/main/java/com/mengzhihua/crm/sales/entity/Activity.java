package com.mengzhihua.crm.sales.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;
import java.time.*;

@Entity @Table(name="crm_activity") @Data
public class Activity extends BaseEntity {
    @Enumerated(EnumType.STRING) private Enums.ActivityType type; private String subject;
    @Column(length=3000) private String content;
    @Enumerated(EnumType.STRING) private Enums.RelatedType relatedType; private Long relatedId; private LocalDateTime dueTime;
    @Enumerated(EnumType.STRING) private Enums.ActivityStatus status; private LocalDateTime completedAt;
}
