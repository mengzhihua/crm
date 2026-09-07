package com.mengzhihua.crm.record.repository;

import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.FieldHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldHistoryRepository extends JpaRepository<FieldHistory, Long> {
    List<FieldHistory> findByTargetTypeAndTargetIdOrderByChangedAtDesc(
            RelatedType targetType,
            Long targetId
    );
}
