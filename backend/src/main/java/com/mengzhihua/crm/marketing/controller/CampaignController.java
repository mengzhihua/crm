package com.mengzhihua.crm.marketing.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.CampaignStatus;
import com.mengzhihua.crm.common.enums.CampaignType;
import com.mengzhihua.crm.marketing.dto.CampaignMemberRequest;
import com.mengzhihua.crm.marketing.entity.Campaign;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import com.mengzhihua.crm.marketing.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "市场活动")
@Validated
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "分页查询市场活动")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<PageResult<Campaign>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CampaignType type,
            @RequestParam(required = false) CampaignStatus status
    ) {
        return Result.ok(campaignService.list(page, size, keyword, type, status));
    }

    @Operation(summary = "查询市场活动")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<Campaign> get(@PathVariable Long id) {
        return Result.ok(campaignService.get(id));
    }

    @Operation(summary = "新增市场活动")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Campaign> add(@Valid @RequestBody Campaign campaign) {
        return Result.ok(campaignService.save(campaign));
    }

    @Operation(summary = "编辑市场活动")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Campaign> edit(
            @PathVariable Long id,
            @Valid @RequestBody Campaign campaign
    ) {
        campaign.setId(id);
        return Result.ok(campaignService.save(campaign));
    }

    @Operation(summary = "删除市场活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查询活动成员")
    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<List<CampaignMember>> members(@PathVariable Long id) {
        return Result.ok(campaignService.members(id));
    }

    @Operation(summary = "批量添加活动成员")
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<List<CampaignMember>> addMembers(
            @PathVariable Long id,
            @Valid @RequestBody CampaignMemberRequest request
    ) {
        return Result.ok(campaignService.addMembers(id, request));
    }

    @Operation(summary = "删除活动成员")
    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public Result<Void> deleteMember(
            @PathVariable Long id,
            @PathVariable Long memberId
    ) {
        campaignService.deleteMember(id, memberId);
        return Result.ok();
    }

    @Operation(summary = "查询活动统计")
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.ok(campaignService.stats(id));
    }
}
