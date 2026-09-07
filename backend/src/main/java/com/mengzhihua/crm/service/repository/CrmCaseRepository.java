package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.service.entity.CrmCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CrmCaseRepository extends JpaRepository<CrmCase, Long>,
        JpaSpecificationExecutor<CrmCase> {
    CrmCase findTopByCaseNoStartingWithOrderByCaseNoDesc(String prefix);

    List<CrmCase> findByAccountId(Long accountId);

    long countByStatusNotIn(Collection<CaseStatus> statuses);

    long countBySlaDueAtBeforeAndStatusNotIn(
            LocalDateTime now,
            Collection<CaseStatus> statuses
    );
}
