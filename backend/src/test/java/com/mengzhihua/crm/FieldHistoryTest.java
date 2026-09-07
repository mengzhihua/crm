package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.FieldHistory;
import com.mengzhihua.crm.record.repository.FieldHistoryRepository;
import com.mengzhihua.crm.sales.dto.OpportunityStageRequest;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.OpportunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class FieldHistoryTest {
    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private FieldHistoryRepository fieldHistoryRepository;

    @BeforeEach
    void setUp() {
        fieldHistoryRepository.deleteAll();
        opportunityRepository.deleteAll();
    }

    @Test
    void recordsOpportunityStageChange() {
        Opportunity opportunity = new Opportunity();
        opportunity.setStage(OpportunityStage.QUALIFICATION);
        opportunity = opportunityService.save(opportunity);

        OpportunityStageRequest request = new OpportunityStageRequest();
        request.setStage(OpportunityStage.PROPOSAL);
        opportunityService.changeStage(opportunity.getId(), request);

        List<FieldHistory> histories = fieldHistoryRepository
                .findByTargetTypeAndTargetIdOrderByChangedAtDesc(
                        RelatedType.OPPORTUNITY,
                        opportunity.getId()
                );
        assertEquals("stage", histories.get(0).getField());
        assertEquals("PROPOSAL", histories.get(0).getNewValue());
    }
}
