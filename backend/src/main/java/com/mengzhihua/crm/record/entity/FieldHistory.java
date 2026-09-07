package com.mengzhihua.crm.record.entity;

import com.mengzhihua.crm.common.enums.RelatedType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_field_history")
public class FieldHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RelatedType targetType;
    private Long targetId;
    private String field;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;

    private String operator;
    private LocalDateTime changedAt;
}
