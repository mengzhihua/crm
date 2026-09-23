package com.mengzhihua.crm.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OpenIrControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OpportunityRepository opportunities;
    @Autowired
    private CrmCaseRepository cases;
    @Autowired
    private CaseService caseService;

    @BeforeEach
    void seed() {
        Opportunity opportunity = opportunities.findAll().stream()
                .filter(item -> "IR-OPP-QUAL".equals(item.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Opportunity created = new Opportunity();
                    created.setName("IR-OPP-QUAL");
                    created.setAmount(new BigDecimal("2400000"));
                    return created;
                });
        opportunity.setStage(OpportunityStage.QUALIFICATION);
        opportunities.save(opportunity);
        CrmCase crmCase = cases.findAll().stream()
                .filter(item -> "CS-IR-NEW".equals(item.getCaseNo()))
                .findFirst()
                .orElseGet(() -> {
                    CrmCase created = new CrmCase();
                    created.setCaseNo("CS-IR-NEW");
                    created.setSubject("交期投诉");
                    created.setPriority(CasePriority.HIGH);
                    created.setType(CaseType.PROBLEM);
                    return created;
                });
        crmCase.setStatus(CaseStatus.NEW);
        crmCase.setPriority(CasePriority.MEDIUM);
        caseService.save(crmCase);
    }

    @Test
    void snapshotsThenAdvanceAndEscalate() throws Exception {
        String snapshots = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "crm-open-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.system").value("CRM"))
                .andReturn().getResponse().getContentAsString();
        JsonNode opp = null;
        JsonNode crmCase = null;
        for (JsonNode row : objectMapper.readTree(snapshots).get("data").get("snapshots")) {
            if ("OPPORTUNITY".equals(row.path("dataType").asText())
                    && "QUALIFICATION".equals(row.path("status").asText())
                    && "IR-OPP-QUAL".equals(row.path("title").asText())) {
                opp = row;
            }
            if ("CASE".equals(row.path("dataType").asText())
                    && "CS-IR-NEW".equals(row.path("bizKey").asText())) {
                crmCase = row;
            }
        }
        assertNotNull(opp, "应包含资格评估商机 IR-OPP-QUAL");
        assertNotNull(crmCase, "应包含待升级工单 CS-IR-NEW");
        assertEquals("NEW", crmCase.path("status").asText());

        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CRM_ADVANCE_STAGE\",\"targetKey\":\"IR-OPP-QUAL\","
                                + "\"idempotencyKey\":\"CRM-ADV-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stage").value("NEEDS_ANALYSIS"));
        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CRM_ADVANCE_STAGE\",\"targetKey\":\"IR-OPP-QUAL\","
                                + "\"idempotencyKey\":\"CRM-ADV-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stage").value("NEEDS_ANALYSIS"));

        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CRM_ESCALATE_CASE\",\"targetKey\":\"CS-IR-NEW\","
                                + "\"idempotencyKey\":\"CRM-ESC-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ESCALATED"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CRM_ESCALATE_CASE\",\"targetKey\":\"CS-IR-NEW\","
                                + "\"idempotencyKey\":\"CRM-ESC-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ESCALATED"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));

        mockMvc.perform(post("/api/open/ir/advance-stage")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opportunityId\":\"IR-OPP-QUAL\",\"idempotencyKey\":\"CRM-ADV-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stage").value("NEEDS_ANALYSIS"));
        mockMvc.perform(post("/api/open/ir/escalate-case")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNo\":\"CS-IR-NEW\",\"idempotencyKey\":\"CRM-ESC-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ESCALATED"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void closeWonAndLostDoNotReuseAdvance() throws Exception {
        Opportunity negotiation = opportunities.findAll().stream()
                .filter(item -> "IR-OPP-NEG".equals(item.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Opportunity created = new Opportunity();
                    created.setName("IR-OPP-NEG");
                    created.setAmount(new BigDecimal("800000"));
                    return created;
                });
        negotiation.setStage(OpportunityStage.NEGOTIATION);
        negotiation.setLostReason(null);
        opportunities.save(negotiation);

        mockMvc.perform(post("/api/open/ir/advance-stage")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opportunityId\":\"IR-OPP-NEG\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/open/ir/close-won")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opportunityId\":\"IR-OPP-NEG\",\"idempotencyKey\":\"CRM-WON-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stage").value("CLOSED_WON"));

        Opportunity lost = opportunities.findAll().stream()
                .filter(item -> "IR-OPP-LOST".equals(item.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Opportunity created = new Opportunity();
                    created.setName("IR-OPP-LOST");
                    created.setAmount(new BigDecimal("100000"));
                    return created;
                });
        lost.setStage(OpportunityStage.NEGOTIATION);
        opportunities.save(lost);

        mockMvc.perform(post("/api/open/ir/close-lost")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opportunityId\":\"IR-OPP-LOST\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/open/ir/close-lost")
                        .header("X-Api-Key", "crm-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opportunityId\":\"IR-OPP-LOST\",\"lostReason\":\"价格\",\"idempotencyKey\":\"CRM-LOST-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stage").value("CLOSED_LOST"));
    }
}
