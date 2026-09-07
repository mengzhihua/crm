package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private CrmCaseRepository caseRepository;

    @BeforeEach
    void setUp() {
        caseRepository.deleteAll();
        opportunityRepository.deleteAll();
        leadRepository.deleteAll();
        accountRepository.deleteAll();

        Account account = new Account();
        account.setName("搜索客户");
        accountRepository.save(account);

        Lead lead = new Lead();
        lead.setName("搜索线索");
        lead.setCompany("搜索公司");
        leadRepository.save(lead);

        Opportunity opportunity = new Opportunity();
        opportunity.setName("搜索商机");
        opportunity.setStage(OpportunityStage.PROPOSAL);
        opportunityRepository.save(opportunity);

        CrmCase crmCase = new CrmCase();
        crmCase.setCaseNo("CSSEARCH001");
        crmCase.setSubject("搜索工单");
        crmCase.setStatus(CaseStatus.NEW);
        caseRepository.save(crmCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSearchIncludesOpportunity() throws Exception {
        mockMvc.perform(
                        get("/api/search")
                                .param("q", "搜索")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.商机[*].type")
                        .value(hasItem("OPPORTUNITY")));
    }

    @Test
    @WithMockUser(roles = "SERVICE_AGENT")
    void serviceAgentCannotSearchOpportunityOrLeadButCanSearchCase()
            throws Exception {
        mockMvc.perform(
                        get("/api/search")
                                .param("q", "搜索")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.商机").doesNotExist())
                .andExpect(jsonPath("$.data.线索").doesNotExist())
                .andExpect(jsonPath("$.data.工单[*].type")
                        .value(hasItem("CASE")))
                .andExpect(jsonPath("$.data.工单[*].type")
                        .value(not(hasItem("OPPORTUNITY"))));
    }
}
