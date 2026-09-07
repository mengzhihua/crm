package com.mengzhihua.crm.approval.repository;

import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long>,
        JpaSpecificationExecutor<ApprovalRequest> {
    List<ApprovalRequest> findByTargetTypeAndTargetIdOrderBySubmittedAtDesc(
            com.mengzhihua.crm.common.enums.ApprovalTargetType targetType,
            Long targetId
    );
}
