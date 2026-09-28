package com.mengzhihua.crm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.repository.ApprovalRequestRepository;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardLayoutTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void savesGetsAndResetsLayoutAndWidgets() throws Exception {
        String body = "{\"widgetsJson\":\"[{\\\"key\\\":\\\"newLeadCount\\\",\\\"span\\\":12}]\"}";
        mockMvc.perform(put("/api/dashboard/layout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        String saved = mockMvc.perform(get("/api/dashboard/layout"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertFalse(objectMapper.readTree(saved)
                .path("data")
                .path("widgetsJson")
                .asText()
                .isEmpty());
        String widgets = mockMvc.perform(get("/api/dashboard/widgets"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode widgetData = objectMapper.readTree(widgets).path("data");
        for (JsonNode widget : widgetData) {
            mockMvc.perform(get("/api/dashboard/widget/" + widget.path("key").asText()))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(delete("/api/dashboard/layout"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminPendingApprovalsIncludeOtherRoles() throws Exception {
        approvalRequestRepository.deleteAll();
        ApprovalRequest request = new ApprovalRequest();
        request.setTargetType(ApprovalTargetType.QUOTE);
        request.setTargetId(1L);
        request.setTitle("经理审批报价");
        request.setSubmitter("sales");
        request.setApproverRole(Role.SALES_MANAGER);
        request.setStatus(ApprovalStatus.PENDING);
        request.setSubmittedAt(LocalDateTime.now());
        approvalRequestRepository.save(request);

        String response = mockMvc.perform(get("/api/dashboard/widget/myPendingApprovals"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode records = objectMapper.readTree(response).path("data");
        assertEquals("经理审批报价", records.get(0).path("title").asText());
    }
}
