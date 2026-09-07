package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.entity.QuoteLineItem;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long>,
        JpaSpecificationExecutor<Quote> {
    Optional<Quote> findTopByQuoteNoStartingWithOrderByQuoteNoDesc(String prefix);

    List<Quote> findByOpportunityId(Long opportunityId);

    List<Quote> findByOpportunityIdAndIdNot(Long opportunityId, Long id);
}
