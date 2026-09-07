package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.ActivityStatus;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.sales.entity.Activity;
import com.mengzhihua.crm.sales.service.ActivityService;
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

@Tag(name = "活动")
@RestController
@RequestMapping("/api/activities")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Operation(summary = "分页查询活动")
    @GetMapping
    public Result<PageResult<Activity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RelatedType relatedType,
            @RequestParam(required = false) Long relatedId,
            @RequestParam(required = false) ActivityStatus status
    ) {
        return Result.ok(activityService.list(
                page, size, keyword, relatedType, relatedId, status
        ));
    }

    @Operation(summary = "查询活动")
    @GetMapping("/{id}")
    public Result<Activity> get(@PathVariable Long id) {
        return Result.ok(activityService.get(id));
    }

    @Operation(summary = "新增活动")
    @PostMapping
    public Result<Activity> add(@RequestBody Activity activity) {
        return Result.ok(activityService.save(activity));
    }

    @Operation(summary = "编辑活动")
    @PutMapping("/{id}")
    public Result<Activity> edit(
            @PathVariable Long id,
            @RequestBody Activity activity
    ) {
        activity.setId(id);
        return Result.ok(activityService.save(activity));
    }

    @Operation(summary = "完成活动")
    @PutMapping("/{id}/complete")
    public Result<Activity> complete(@PathVariable Long id) {
        return Result.ok(activityService.complete(id));
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.ok();
    }
}
