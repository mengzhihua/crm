package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.OpportunityLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpportunityLineItemRepository extends JpaRepository<OpportunityLineItem, Long> {
    List<OpportunityLineItem> findByOpportunityId(Long opportunityId);

    void deleteByOpportunityId(Long opportunityId);
}
