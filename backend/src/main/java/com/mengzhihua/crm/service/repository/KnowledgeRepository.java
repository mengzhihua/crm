package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface KnowledgeRepository extends JpaRepository<KnowledgeArticle, Long>,
        JpaSpecificationExecutor<KnowledgeArticle> {
}
