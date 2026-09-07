package com.mengzhihua.crm.service.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_case_comment")
public class CaseComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long caseId;
    private String author;

    @Column(length = 3000)
    private String content;

    private Boolean internal;
    private LocalDateTime createdAt;

    @PrePersist
    public void init() {
        createdAt = LocalDateTime.now();
    }
}
