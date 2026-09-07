package com.mengzhihua.crm;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.dto.OpportunityStageRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.OpportunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class OpportunityServiceTest {
    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private OpportunityService opportunityService;

    @BeforeEach
    void setUp() {
        opportunityRepository.deleteAll();
    }

    @Test
    void usesStageDefaultProbability() {
        Opportunity opportunity = new Opportunity();
        Opportunity saved = opportunityService.save(opportunity);

        assertEquals(
                OpportunityStage.QUALIFICATION.getDefaultProbability(),
                saved.getProbability()
        );
    }

    @Test
    void requiresLostReason() {
        Opportunity opportunity = opportunityService.save(new Opportunity());
        Long opportunityId = opportunity.getId();
        OpportunityStageRequest request = new OpportunityStageRequest();
        request.setStage(OpportunityStage.CLOSED_LOST);

        assertThrows(
                BizException.class,
                () -> opportunityService.changeStage(opportunityId, request)
        );
    }
}
