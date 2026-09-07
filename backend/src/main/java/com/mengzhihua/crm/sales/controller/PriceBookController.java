package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.sales.entity.PriceBook;
import com.mengzhihua.crm.sales.entity.PriceBookEntry;
import com.mengzhihua.crm.sales.service.PriceBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "价格手册")
@RestController
@RequestMapping("/api/pricebooks")
public class PriceBookController {
    private final PriceBookService priceBookService;

    public PriceBookController(PriceBookService priceBookService) {
        this.priceBookService = priceBookService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<PageResult<PriceBook>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.ok(priceBookService.list(page, size, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<PriceBook> get(@PathVariable Long id) {
        return Result.ok(priceBookService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<PriceBook> add(@RequestBody PriceBook priceBook) {
        return Result.ok(priceBookService.save(priceBook));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<PriceBook> edit(
            @PathVariable Long id,
            @RequestBody PriceBook priceBook
    ) {
        priceBook.setId(id);
        return Result.ok(priceBookService.save(priceBook));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> delete(@PathVariable Long id) {
        priceBookService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/entries")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<List<PriceBookEntry>> entries(@PathVariable Long id) {
        return Result.ok(priceBookService.entries(id));
    }

    @PostMapping("/{id}/entries")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<PriceBookEntry> addEntry(
            @PathVariable Long id,
            @RequestBody PriceBookEntry entry
    ) {
        return Result.ok(priceBookService.saveEntry(id, entry));
    }

    @PutMapping("/{id}/entries/{entryId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<PriceBookEntry> editEntry(
            @PathVariable Long id,
            @PathVariable Long entryId,
            @RequestBody PriceBookEntry entry
    ) {
        entry.setId(entryId);
        return Result.ok(priceBookService.saveEntry(id, entry));
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> deleteEntry(
            @PathVariable Long id,
            @PathVariable Long entryId
    ) {
        priceBookService.deleteEntry(entryId);
        return Result.ok();
    }
}
