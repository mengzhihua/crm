package com.mengzhihua.crm.record.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.FieldHistory;
import com.mengzhihua.crm.record.repository.FieldHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FieldHistoryService {
    private final FieldHistoryRepository repository;

    public FieldHistoryService(FieldHistoryRepository repository) {
        this.repository = repository;
    }

    public FieldHistory record(
            RelatedType targetType,
            Long targetId,
            String field,
            Object oldValue,
            Object newValue
    ) {
        if (java.util.Objects.equals(oldValue, newValue)) {
            return null;
        }
        FieldHistory history = new FieldHistory();
        history.setTargetType(targetType);
        history.setTargetId(targetId);
        history.setField(field);
        history.setOldValue(String.valueOf(oldValue));
        history.setNewValue(String.valueOf(newValue));
        history.setOperator(CurrentUser.usernameOrDefault());
        history.setChangedAt(LocalDateTime.now());
        return repository.save(history);
    }

    public List<FieldHistory> list(RelatedType targetType, Long targetId) {
        return repository.findByTargetTypeAndTargetIdOrderByChangedAtDesc(
                targetType,
                targetId
        );
    }
}
