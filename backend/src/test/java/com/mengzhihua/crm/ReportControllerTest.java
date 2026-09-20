package com.mengzhihua.crm;

import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CrmCaseRepository caseRepository;

    @Autowired
    private LeadRepository leadRepository;

    @BeforeEach
    void setUp() {
        caseRepository.deleteAll();
        leadRepository.deleteAll();
        CrmCase crmCase = new CrmCase();
        crmCase.setCaseNo("RP-1");
        crmCase.setPriority(CasePriority.HIGH);
        crmCase.setStatus(CaseStatus.NEW);
        caseRepository.save(crmCase);
        Lead lead = new Lead();
        lead.setName("报表线索");
        lead.setStatus(LeadStatus.NEW);
        leadRepository.save(lead);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void allReportsReturnSuccessForAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/sales-funnel")
        )
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/sales-performance")
        )
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/case-analysis")
        )
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/campaign-roi")
        )
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/receivables")
        )
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/sales-funnel/export"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("\"线索\",\"NEW\",\"1\",\"\"")
                ));
    }
}
