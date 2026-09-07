package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.PriceBookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PriceBookEntryRepository extends JpaRepository<PriceBookEntry, Long>,
        JpaSpecificationExecutor<PriceBookEntry> {
    List<PriceBookEntry> findByPriceBookId(Long priceBookId);

    Optional<PriceBookEntry> findByPriceBookIdAndProductId(Long priceBookId, Long productId);
}
