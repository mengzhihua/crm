package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>,
        JpaSpecificationExecutor<Opportunity> {
    List<Opportunity> findByAccountId(Long accountId);

    List<Opportunity> findByStage(OpportunityStage stage);

    long countByStageNotIn(Collection<OpportunityStage> stages);

    List<Opportunity> findByStageNotIn(Collection<OpportunityStage> stages);
}
