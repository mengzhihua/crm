package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.sales.dto.LineItemsRequest;
import com.mengzhihua.crm.sales.entity.OpportunityLineItem;
import com.mengzhihua.crm.sales.service.OpportunityItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "商机产品")
@RestController
@RequestMapping("/api/opportunities/{opportunityId}/items")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
public class OpportunityItemController {
    private final OpportunityItemService itemService;

    public OpportunityItemController(OpportunityItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "查询商机产品")
    @GetMapping
    public Result<List<OpportunityLineItem>> list(
            @PathVariable Long opportunityId
    ) {
        return Result.ok(itemService.list(opportunityId));
    }

    @Operation(summary = "整体替换商机产品")
    @PutMapping
    public Result<List<OpportunityLineItem>> replace(
            @PathVariable Long opportunityId,
            @Valid @RequestBody LineItemsRequest request
    ) {
        return Result.ok(itemService.replace(opportunityId, request));
    }
}
