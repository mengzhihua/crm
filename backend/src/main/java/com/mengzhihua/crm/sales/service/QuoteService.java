package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.approval.service.ApprovalService;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.SerialNumberGenerator;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.service.FieldHistoryService;
import com.mengzhihua.crm.sales.dto.LineItemRequest;
import com.mengzhihua.crm.sales.dto.LineItemsRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.entity.OpportunityLineItem;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.entity.QuoteLineItem;
import com.mengzhihua.crm.sales.repository.OpportunityLineItemRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.repository.QuoteLineItemRepository;
import com.mengzhihua.crm.sales.repository.QuoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final QuoteLineItemRepository lineItemRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityLineItemRepository opportunityItemRepository;
    private final ApprovalService approvalService;
    private final FieldHistoryService fieldHistoryService;

    public QuoteService(
            QuoteRepository quoteRepository,
            QuoteLineItemRepository lineItemRepository,
            OpportunityRepository opportunityRepository,
            OpportunityLineItemRepository opportunityItemRepository,
            ApprovalService approvalService,
            FieldHistoryService fieldHistoryService
    ) {
        this.quoteRepository = quoteRepository;
        this.lineItemRepository = lineItemRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityItemRepository = opportunityItemRepository;
        this.approvalService = approvalService;
        this.fieldHistoryService = fieldHistoryService;
    }

    public PageResult<Quote> list(
            int page,
            int size,
            String keyword,
            QuoteStatus status,
            Long opportunityId
    ) {
        Specification<Quote> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (opportunityId != null) {
                predicates.add(builder.equal(root.get("opportunityId"), opportunityId));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Quote> result = quoteRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Quote) item);
    }

    public Quote get(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new BizException("报价单不存在"));
    }

    public List<QuoteLineItem> items(Long quoteId) {
        get(quoteId);
        return lineItemRepository.findByQuoteId(quoteId);
    }

    public Quote save(Quote quote) {
        if (quote.getId() == null) {
            quote.setQuoteNo(nextNo());
            quote.setStatus(QuoteStatus.DRAFT);
            quote.setSubtotal(BigDecimal.ZERO);
            quote.setTotalAmount(BigDecimal.ZERO);
        } else {
            Quote current = get(quote.getId());
            ensureEditable(current);
            quote.setQuoteNo(current.getQuoteNo());
            quote.setStatus(current.getStatus());
            quote.setSubtotal(current.getSubtotal());
            quote.setTotalAmount(current.getTotalAmount());
        }
        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote fromOpportunity(Long opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new BizException("商机不存在"));
        Quote quote = new Quote();
        quote.setQuoteNo(nextNo());
        quote.setOpportunityId(opportunity.getId());
        quote.setAccountId(opportunity.getAccountId());
        quote.setContactId(opportunity.getContactId());
        quote.setPriceBookId(opportunity.getPriceBookId());
        quote.setName(opportunity.getName() + "报价单");
        quote.setValidUntil(LocalDate.now().plusDays(30));
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setDiscountRate(BigDecimal.ZERO);
        quote.setPrimaryQuote(false);
        quote = quoteRepository.save(quote);
        List<OpportunityLineItem> source = opportunityItemRepository
                .findByOpportunityId(opportunityId);
        List<QuoteLineItem> copied = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OpportunityLineItem sourceItem : source) {
            QuoteLineItem item = new QuoteLineItem();
            item.setQuoteId(quote.getId());
            item.setProductId(sourceItem.getProductId());
            item.setPriceBookEntryId(sourceItem.getPriceBookEntryId());
            item.setQuantity(sourceItem.getQuantity());
            item.setUnitPrice(sourceItem.getUnitPrice());
            item.setDiscountRate(sourceItem.getDiscountRate());
            item.setTotalPrice(sourceItem.getTotalPrice());
            subtotal = subtotal.add(item.getTotalPrice());
            copied.add(item);
        }
        lineItemRepository.saveAll(copied);
        quote.setSubtotal(subtotal);
        quote.setTotalAmount(totalAmount(subtotal, quote.getDiscountRate()));
        return quoteRepository.save(quote);
    }

    @Transactional
    public List<QuoteLineItem> replaceItems(Long quoteId, LineItemsRequest request) {
        Quote quote = get(quoteId);
        ensureEditable(quote);
        lineItemRepository.deleteByQuoteId(quoteId);
        List<QuoteLineItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineItemRequest input : request.getItems()) {
            QuoteLineItem item = new QuoteLineItem();
            item.setQuoteId(quoteId);
            item.setProductId(input.getProductId());
            item.setPriceBookEntryId(input.getPriceBookEntryId());
            item.setQuantity(input.getQuantity());
            item.setUnitPrice(input.getUnitPrice());
            item.setDiscountRate(OpportunityItemService.normalizeDiscount(
                    input.getDiscountRate()
            ));
            item.setTotalPrice(OpportunityItemService.total(
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscountRate()
            ));
            subtotal = subtotal.add(item.getTotalPrice());
            items.add(item);
        }
        quote.setSubtotal(subtotal);
        quote.setTotalAmount(totalAmount(subtotal, quote.getDiscountRate()));
        quoteRepository.save(quote);
        return lineItemRepository.saveAll(items);
    }

    public Quote updateDiscount(Long id, BigDecimal discountRate) {
        Quote quote = get(id);
        ensureEditable(quote);
        quote.setDiscountRate(discountRate);
        quote.setTotalAmount(totalAmount(quote.getSubtotal(), discountRate));
        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote submit(Long id) {
        Quote quote = get(id);
        QuoteStatus oldStatus = quote.getStatus();
        if (quote.getStatus() != QuoteStatus.DRAFT
                && quote.getStatus() != QuoteStatus.REJECTED) {
            throw new BizException("当前报价单不可提交审批");
        }
        boolean required = approvalService.submit(
                ApprovalTargetType.QUOTE,
                quote.getId(),
                quote.getTotalAmount(),
                quote.getDiscountRate(),
                quote.getName()
        );
        quote.setStatus(required ? QuoteStatus.IN_REVIEW : QuoteStatus.APPROVED);
        Quote saved = quoteRepository.save(quote);
        fieldHistoryService.record(
                RelatedType.QUOTE,
                id,
                "status",
                oldStatus,
                saved.getStatus()
        );
        return saved;
    }

    @Transactional
    public Quote accept(Long id) {
        Quote quote = get(id);
        QuoteStatus oldStatus = quote.getStatus();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            throw new BizException("只有已批准报价单可以接受");
        }
        List<Quote> others = quoteRepository.findByOpportunityIdAndIdNot(
                quote.getOpportunityId(),
                id
        );
        for (Quote other : others) {
            other.setPrimaryQuote(false);
        }
        quoteRepository.saveAll(others);
        quote.setPrimaryQuote(true);
        quote.setStatus(QuoteStatus.ACCEPTED);
        Opportunity opportunity = opportunityRepository.findById(
                quote.getOpportunityId()
        ).orElseThrow(() -> new BizException("商机不存在"));
        opportunity.setAmount(quote.getTotalAmount());
        if (opportunity.getStage() == null
                || opportunity.getStage().ordinal() < OpportunityStage.NEGOTIATION.ordinal()) {
            opportunity.setStage(OpportunityStage.NEGOTIATION);
            opportunity.setProbability(
                    OpportunityStage.NEGOTIATION.getDefaultProbability()
            );
        }
        opportunityRepository.save(opportunity);
        Quote saved = quoteRepository.save(quote);
        fieldHistoryService.record(
                RelatedType.QUOTE,
                id,
                "status",
                oldStatus,
                saved.getStatus()
        );
        return saved;
    }

    private String nextNo() {
        String prefix = "QT" + LocalDate.now().format(
                DateTimeFormatter.BASIC_ISO_DATE
        );
        Quote latest = quoteRepository
                .findTopByQuoteNoStartingWithOrderByQuoteNoDesc(prefix)
                .orElse(null);
        return SerialNumberGenerator.next(
                prefix,
                latest == null ? null : latest.getQuoteNo()
        );
    }

    private void ensureEditable(Quote quote) {
        if (quote.getStatus() != QuoteStatus.DRAFT
                && quote.getStatus() != QuoteStatus.REJECTED) {
            throw new BizException("当前报价单不可编辑");
        }
    }

    private BigDecimal totalAmount(BigDecimal subtotal, BigDecimal discountRate) {
        return OpportunityItemService.total(
                BigDecimal.ONE,
                subtotal,
                discountRate == null ? BigDecimal.ZERO : discountRate
        );
    }
}
