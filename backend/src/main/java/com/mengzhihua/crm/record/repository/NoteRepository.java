package com.mengzhihua.crm.record.repository;

import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            RelatedType targetType,
            Long targetId
    );
}
