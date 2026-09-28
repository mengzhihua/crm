package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.common.enums.DataScope;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.permission.entity.RoleDataScope;
import com.mengzhihua.crm.permission.repository.RecordShareRepository;
import com.mengzhihua.crm.permission.repository.RoleDataScopeRepository;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataScopeShareTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private RecordShareRepository shareRepository;

    @Autowired
    private RoleDataScopeRepository scopeRepository;

    @BeforeEach
    void setUp() {
        shareRepository.deleteAll();
        opportunityRepository.deleteAll();
        RoleDataScope scope = scopeRepository
                .findByRoleAndObjectType(Role.SALES_REP, DataObjectType.OPPORTUNITY)
                .orElseGet(RoleDataScope::new);
        scope.setRole(Role.SALES_REP);
        scope.setObjectType(DataObjectType.OPPORTUNITY);
        scope.setScope(DataScope.OWN);
        scope.setOwner("admin");
        scopeRepository.save(scope);
    }

    @Test
    @WithMockUser(username = "sales", roles = "SALES_REP")
    void cannotListSharesForRecordOutsideScope() throws Exception {
        Opportunity opportunity = new Opportunity();
        opportunity.setName("经理商机");
        opportunity.setOwner("manager");
        opportunity.setStage(OpportunityStage.NEGOTIATION);
        opportunity.setAmount(new BigDecimal("100"));
        opportunity = opportunityRepository.save(opportunity);

        int status = mockMvc.perform(get("/api/shares")
                        .param("objectType", "OPPORTUNITY")
                        .param("recordId", opportunity.getId().toString()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(status == 400 || status == 403, "status=" + status);
    }
}
