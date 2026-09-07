package com.mengzhihua.crm.common;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected String owner;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); updatedAt = createdAt; if (owner == null) owner = "系统管理员"; }
    @PreUpdate public void preUpdate() { updatedAt = LocalDateTime.now(); }
}
