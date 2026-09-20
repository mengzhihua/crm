package com.mengzhihua.crm.approval.repository;

import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long>,
        JpaSpecificationExecutor<ApprovalRequest> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from ApprovalRequest item where item.id = :id")
    Optional<ApprovalRequest> findWithLockById(@Param("id") Long id);

    List<ApprovalRequest> findByTargetTypeAndTargetIdOrderBySubmittedAtDesc(
            com.mengzhihua.crm.common.enums.ApprovalTargetType targetType,
            Long targetId
    );
}
