package com.mengzhihua.crm.approval.repository;

import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, Long> {
    List<ApprovalRule> findByTargetTypeAndActiveTrueOrderByPriorityAsc(
            ApprovalTargetType targetType
    );
}
