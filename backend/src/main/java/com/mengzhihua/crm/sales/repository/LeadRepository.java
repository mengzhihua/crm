package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.sales.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeadRepository extends JpaRepository<Lead, Long>,
        JpaSpecificationExecutor<Lead> {
    long countByStatus(LeadStatus status);
}
