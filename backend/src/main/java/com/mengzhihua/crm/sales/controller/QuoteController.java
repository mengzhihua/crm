package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.sales.dto.LineItemsRequest;
import com.mengzhihua.crm.sales.dto.QuoteDiscountRequest;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.entity.QuoteLineItem;
import com.mengzhihua.crm.sales.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "报价单")
@RestController
@RequestMapping("/api/quotes")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
public class QuoteController {
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    public Result<PageResult<Quote>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) QuoteStatus status,
            @RequestParam(required = false) Long opportunityId
    ) {
        return Result.ok(quoteService.list(
                page, size, keyword, status, opportunityId
        ));
    }

    @GetMapping("/{id}")
    public Result<Quote> get(@PathVariable Long id) {
        return Result.ok(quoteService.get(id));
    }

    @PostMapping
    public Result<Quote> add(@RequestBody Quote quote) {
        return Result.ok(quoteService.save(quote));
    }

    @PutMapping("/{id}")
    public Result<Quote> edit(
            @PathVariable Long id,
            @RequestBody Quote quote
    ) {
        quote.setId(id);
        return Result.ok(quoteService.save(quote));
    }

    @PostMapping("/from-opportunity/{opportunityId}")
    public Result<Quote> fromOpportunity(@PathVariable Long opportunityId) {
        return Result.ok(quoteService.fromOpportunity(opportunityId));
    }

    @GetMapping("/{id}/items")
    public Result<List<QuoteLineItem>> items(@PathVariable Long id) {
        return Result.ok(quoteService.items(id));
    }

    @PutMapping("/{id}/items")
    public Result<List<QuoteLineItem>> replaceItems(
            @PathVariable Long id,
            @Valid @RequestBody LineItemsRequest request
    ) {
        return Result.ok(quoteService.replaceItems(id, request));
    }

    @PutMapping("/{id}/discount")
    public Result<Quote> discount(
            @PathVariable Long id,
            @Valid @RequestBody QuoteDiscountRequest request
    ) {
        return Result.ok(quoteService.updateDiscount(
                id,
                request.getDiscountRate()
        ));
    }

    @PostMapping("/{id}/submit")
    public Result<Quote> submit(@PathVariable Long id) {
        return Result.ok(quoteService.submit(id));
    }

    @PostMapping("/{id}/accept")
    public Result<Quote> accept(@PathVariable Long id) {
        return Result.ok(quoteService.accept(id));
    }
}
