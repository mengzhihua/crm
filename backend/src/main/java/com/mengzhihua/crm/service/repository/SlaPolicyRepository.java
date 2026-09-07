package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.service.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    SlaPolicy findFirstByPriorityAndActiveTrue(CasePriority priority);

    List<SlaPolicy> findByActiveTrueOrderByPriorityAsc();
}
