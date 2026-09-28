package com.mengzhihua.crm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
                .getContentAsString();
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
}
