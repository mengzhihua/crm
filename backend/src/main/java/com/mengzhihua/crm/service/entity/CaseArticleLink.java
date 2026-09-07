package com.mengzhihua.crm.service.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@Table(
        name = "crm_case_article_link",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"caseId", "articleId"}
        )
)
public class CaseArticleLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long caseId;
    private Long articleId;
}
