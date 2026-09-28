package com.mengzhihua.crm.permission.repository;

import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.permission.entity.RecordShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordShareRepository extends JpaRepository<RecordShare, Long> {
    List<RecordShare> findByObjectTypeAndRecordId(
            DataObjectType objectType,
            Long recordId
    );

    Optional<RecordShare> findByObjectTypeAndRecordIdAndSharedWith(
            DataObjectType objectType,
            Long recordId,
            String sharedWith
    );
}
