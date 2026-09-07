package com.mengzhihua.crm.record.entity;

import com.mengzhihua.crm.common.enums.RelatedType;
import lombok.Data;

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
@Table(name = "crm_attachment")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RelatedType targetType;
    private Long targetId;
    private String fileName;
    private String storageName;
    private Long size;
    private String contentType;
    private String uploader;
    private LocalDateTime createdAt;

    public Attachment() {
        createdAt = LocalDateTime.now();
    }
}
