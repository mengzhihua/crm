package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.service.entity.CaseComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseCommentRepository extends JpaRepository<CaseComment, Long> {
    List<CaseComment> findByCaseIdOrderByCreatedAtAsc(Long id);
}
