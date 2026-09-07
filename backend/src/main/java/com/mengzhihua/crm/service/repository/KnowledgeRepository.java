package com.mengzhihua.crm.service.repository;
import com.mengzhihua.crm.service.entity.KnowledgeArticle; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface KnowledgeRepository extends JpaRepository<KnowledgeArticle,Long> { Page<KnowledgeArticle> findByTitleContainingIgnoreCaseOrKeywordsContainingIgnoreCaseOrContentContainingIgnoreCase(String a,String b,String c,Pageable p); }
