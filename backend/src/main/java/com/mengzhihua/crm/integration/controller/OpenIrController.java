package com.mengzhihua.crm.integration.controller;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.dto.OpportunityStageRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.OpportunityService;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.service.CaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** IR 控制塔：商机 / 工单快照，推进商机阶段或升级工单。 */
@RestController
@RequestMapping("/api/open/ir")
public class OpenIrController {
    private final OpportunityRepository opportunities;
    private final OpportunityService opportunityService;
    private final CrmCaseRepository cases;
    private final CaseService caseService;
    private final String apiKey;
    private final ConcurrentHashMap<String, Object> actionCache = new ConcurrentHashMap<String, Object>();

    public OpenIrController(
            OpportunityRepository opportunities,
            OpportunityService opportunityService,
            CrmCaseRepository cases,
            CaseService caseService,
            @Value("${crm.open.api-key:crm-open-key}") String apiKey) {
        this.opportunities = opportunities;
        this.opportunityService = opportunityService;
        this.cases = cases;
        this.caseService = caseService;
        this.apiKey = apiKey;
    }

    @GetMapping("/snapshots")
    public Result<Map<String, Object>> snapshots(
            @RequestHeader(value = "X-Api-Key", required = false) String key) {
        checkKey(key);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Opportunity opportunity : opportunities.findAll()) {
            rows.add(row("OPPORTUNITY", String.valueOf(opportunity.getId()),
                    opportunity.getStage() == null ? null : opportunity.getStage().name(),
                    opportunity.getName(), BigDecimal.ONE, opportunity.getAmount(),
                    null, opportunity.getName()));
        }
        for (CrmCase crmCase : cases.findAll()) {
            rows.add(row("CASE", crmCase.getCaseNo(),
                    crmCase.getStatus() == null ? null : crmCase.getStatus().name(),
                    crmCase.getSubject(), BigDecimal.ONE, null, null, crmCase.getSubject()));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("system", "CRM");
        data.put("snapshots", rows);
        return Result.ok(data);
    }

    @PostMapping("/actions")
    public Result<Object> actions(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        checkKey(key);
        String type = String.valueOf(body.getOrDefault("type", ""));
        String targetKey = String.valueOf(body.getOrDefault("targetKey", ""));
        return Result.ok(executeOnce(cacheKey(type, targetKey, body.get("idempotencyKey")), () -> {
            if ("CRM_ADVANCE_STAGE".equals(type)) {
                Opportunity opportunity = opportunityOf(targetKey);
                OpportunityStage next = nextStage(opportunity.getStage());
                OpportunityStageRequest request = new OpportunityStageRequest();
                request.setStage(next);
                return opportunityService.changeStage(opportunity.getId(), request);
            }
            if ("CRM_ESCALATE_CASE".equals(type)) {
                CrmCase crmCase = cases.findAll().stream()
                        .filter(item -> targetKey.equals(item.getCaseNo())
                                || targetKey.equals(String.valueOf(item.getId())))
                        .findFirst()
                        .orElseThrow(() -> new BizException("工单不存在: " + targetKey));
                return caseService.escalate(crmCase.getId());
            }
            throw new BizException("不支持的 IR 指令: " + type);
        }));
    }

    private Object executeOnce(String cacheKey, Supplier<Object> work) {
        if (cacheKey == null) {
            return work.get();
        }
        Object cached = actionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (actionCache) {
            cached = actionCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Object created = work.get();
            actionCache.put(cacheKey, created);
            return created;
        }
    }

    private static String cacheKey(String type, String targetKey, Object idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String key = String.valueOf(idempotencyKey).trim();
        if (key.isEmpty() || "null".equals(key)) {
            return null;
        }
        return type + "|" + (targetKey == null ? "" : targetKey) + "|" + key;
    }

    private Opportunity opportunityOf(String targetKey) {
        if (targetKey == null || targetKey.trim().isEmpty() || "null".equals(targetKey)) {
            throw new BizException("商机ID 必填");
        }
        String value = targetKey.trim();
        try {
            return opportunityService.get(Long.valueOf(value));
        } catch (NumberFormatException ignored) {
            return opportunities.findAll().stream()
                    .filter(item -> value.equals(item.getName())
                            || value.equals(String.valueOf(item.getId())))
                    .findFirst()
                    .orElseThrow(() -> new BizException("商机不存在: " + value));
        }
    }

    private OpportunityStage nextStage(OpportunityStage stage) {
        if (stage == null || stage == OpportunityStage.QUALIFICATION) {
            return OpportunityStage.NEEDS_ANALYSIS;
        }
        if (stage == OpportunityStage.NEEDS_ANALYSIS) {
            return OpportunityStage.PROPOSAL;
        }
        if (stage == OpportunityStage.PROPOSAL) {
            return OpportunityStage.NEGOTIATION;
        }
        if (stage == OpportunityStage.NEGOTIATION) {
            return OpportunityStage.CLOSED_WON;
        }
        throw new BizException("商机已关闭，无法推进");
    }

    private void checkKey(String key) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.equals(key)) {
            throw new BizException("无效的 API Key");
        }
    }

    private static Map<String, Object> row(
            String dataType, String bizKey, String status, String sku,
            BigDecimal qty, BigDecimal amount, String plantCode, String title) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dataType", dataType);
        row.put("bizKey", bizKey);
        row.put("status", status);
        row.put("sku", sku);
        row.put("qty", qty);
        row.put("amount", amount);
        row.put("plantCode", plantCode);
        row.put("title", title);
        return row;
    }
}
