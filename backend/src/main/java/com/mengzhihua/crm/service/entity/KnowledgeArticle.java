package com.mengzhihua.crm.service.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ArticleStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_knowledge")
public class KnowledgeArticle extends BaseEntity {
    private String title;
    private String category;
    private String keywords;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;
    private Long viewCount;
}
