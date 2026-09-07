package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.service.entity.AssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRuleRepository extends JpaRepository<AssignmentRule, Long> {
    List<AssignmentRule> findByActiveTrueOrderByPriorityAsc();
}
