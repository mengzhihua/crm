package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.service.entity.CaseArticleLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseArticleLinkRepository extends JpaRepository<CaseArticleLink, Long> {
    List<CaseArticleLink> findByCaseId(Long caseId);

    Optional<CaseArticleLink> findByCaseIdAndArticleId(
            Long caseId,
            Long articleId
    );
}
