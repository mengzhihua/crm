package com.mengzhihua.crm;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.sales.dto.LeadConvertRequest;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class LeadServiceTest {
    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadRepository.deleteAll();
    }

    @Test
    void convertsLeadOnce() {
        Lead lead = new Lead();
        lead.setName("张三");
        lead.setCompany("测试公司");
        lead = leadRepository.save(lead);

        LeadConvertRequest request = new LeadConvertRequest();
        request.setCreateOpportunity(true);
        request.setOpportunityName("测试商机");
        Long leadId = lead.getId();

        leadService.convert(leadId, request);

        assertEquals(LeadStatus.CONVERTED, leadService.get(leadId).getStatus());
        assertThrows(
                BizException.class,
                () -> leadService.convert(leadId, request)
        );
    }
}
