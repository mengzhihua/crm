package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.sales.dto.LineItemRequest;
import com.mengzhihua.crm.sales.dto.LineItemsRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.entity.OpportunityLineItem;
import com.mengzhihua.crm.sales.repository.OpportunityLineItemRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpportunityItemService {
    private final OpportunityRepository opportunityRepository;
    private final OpportunityLineItemRepository itemRepository;

    public OpportunityItemService(
            OpportunityRepository opportunityRepository,
            OpportunityLineItemRepository itemRepository
    ) {
        this.opportunityRepository = opportunityRepository;
        this.itemRepository = itemRepository;
    }

    public List<OpportunityLineItem> list(Long opportunityId) {
        getOpportunity(opportunityId);
        return itemRepository.findByOpportunityId(opportunityId);
    }

    @Transactional
    public List<OpportunityLineItem> replace(
            Long opportunityId,
            LineItemsRequest request
    ) {
        Opportunity opportunity = getOpportunity(opportunityId);
        itemRepository.deleteByOpportunityId(opportunityId);
        List<OpportunityLineItem> items = new ArrayList<>();
        BigDecimal amount = BigDecimal.ZERO;
        for (LineItemRequest input : request.getItems()) {
            OpportunityLineItem item = new OpportunityLineItem();
            item.setOpportunityId(opportunityId);
            item.setProductId(input.getProductId());
            item.setPriceBookEntryId(input.getPriceBookEntryId());
            item.setQuantity(input.getQuantity());
            item.setUnitPrice(input.getUnitPrice());
            item.setDiscountRate(normalizeDiscount(input.getDiscountRate()));
            item.setTotalPrice(total(input.getQuantity(), input.getUnitPrice(),
                    item.getDiscountRate()));
            amount = amount.add(item.getTotalPrice());
            items.add(item);
        }
        opportunity.setAmount(amount);
        opportunityRepository.save(opportunity);
        return itemRepository.saveAll(items);
    }

    private Opportunity getOpportunity(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new BizException("商机不存在"));
    }

    static BigDecimal normalizeDiscount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    static BigDecimal total(
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountRate
    ) {
        BigDecimal factor = BigDecimal.ONE.subtract(
                discountRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
        );
        return quantity.multiply(unitPrice).multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
