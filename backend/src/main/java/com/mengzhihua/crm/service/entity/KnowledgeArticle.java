package com.mengzhihua.crm.service.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;

@Entity @Table(name="crm_knowledge") @Data
public class KnowledgeArticle extends BaseEntity {
    private String title; private String category; private String keywords;
    @Lob @Column(columnDefinition="CLOB") private String content;
    @Enumerated(EnumType.STRING) private Enums.ArticleStatus status; private Long viewCount;
}
