package com.mengzhihua.crm.record.repository;

import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            RelatedType targetType,
            Long targetId
    );
}
