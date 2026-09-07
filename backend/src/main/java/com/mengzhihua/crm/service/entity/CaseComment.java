package com.mengzhihua.crm.service.entity;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="crm_case_comment") @Data
public class CaseComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long caseId; private String author; @Column(length=3000) private String content; private Boolean internal; private LocalDateTime createdAt;
    @PrePersist public void init() { createdAt = LocalDateTime.now(); }
}
