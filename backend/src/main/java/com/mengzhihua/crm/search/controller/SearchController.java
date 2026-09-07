package com.mengzhihua.crm.search.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "全局搜索")
@RestController
@RequestMapping("/api/search")
@PreAuthorize("isAuthenticated()")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @Operation(summary = "全局搜索")
    @GetMapping
    public Result<Map<String, List<Map<String, Object>>>> search(
            @RequestParam String q
    ) {
        return Result.ok(searchService.search(q));
    }
}
