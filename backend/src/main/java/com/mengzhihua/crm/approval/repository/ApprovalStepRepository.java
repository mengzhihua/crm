package com.mengzhihua.crm.approval.repository;

import com.mengzhihua.crm.approval.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);
}
