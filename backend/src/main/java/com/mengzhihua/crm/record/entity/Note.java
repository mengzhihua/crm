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
@Table(name = "crm_note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RelatedType targetType;
    private Long targetId;

    @Column(length = 4000)
    private String content;

    private String author;
    private LocalDateTime createdAt;

    public Note() {
        createdAt = LocalDateTime.now();
    }
}
