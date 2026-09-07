package com.mengzhihua.crm.service.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.ArticleStatus;
import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import com.mengzhihua.crm.service.repository.KnowledgeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeService {
    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    public PageResult<KnowledgeArticle> list(
            int page,
            int size,
            String keyword,
            ArticleStatus status
    ) {
        Specification<KnowledgeArticle> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), value),
                        builder.like(builder.lower(root.get("keywords")), value),
                        builder.like(builder.lower(root.get("content")), value)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<KnowledgeArticle> result = knowledgeRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (KnowledgeArticle) item);
    }

    public KnowledgeArticle get(Long id) {
        KnowledgeArticle article = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BizException("文章不存在"));
        article.setViewCount(
                (article.getViewCount() == null ? 0 : article.getViewCount()) + 1
        );
        return knowledgeRepository.save(article);
    }

    public KnowledgeArticle save(KnowledgeArticle article) {
        if (article.getStatus() == null) {
            article.setStatus(ArticleStatus.DRAFT);
        }
        if (article.getViewCount() == null) {
            article.setViewCount(0L);
        }
        return knowledgeRepository.save(article);
    }

    public void delete(Long id) {
        knowledgeRepository.deleteById(id);
    }

    public KnowledgeArticle publish(Long id) {
        KnowledgeArticle article = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BizException("文章不存在"));
        article.setStatus(ArticleStatus.PUBLISHED);
        return knowledgeRepository.save(article);
    }
}
