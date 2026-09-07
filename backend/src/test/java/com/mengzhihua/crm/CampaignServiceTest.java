package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.CampaignStatus;
import com.mengzhihua.crm.common.enums.CampaignType;
import com.mengzhihua.crm.common.enums.MemberStatus;
import com.mengzhihua.crm.common.enums.MemberType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.marketing.entity.Campaign;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import com.mengzhihua.crm.marketing.repository.CampaignMemberRepository;
import com.mengzhihua.crm.marketing.repository.CampaignRepository;
import com.mengzhihua.crm.marketing.service.CampaignService;
import com.mengzhihua.crm.sales.dto.LeadConvertRequest;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CampaignServiceTest {
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignMemberRepository memberRepository;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        opportunityRepository.deleteAll();
        contactRepository.deleteAll();
        accountRepository.deleteAll();
        leadRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @Test
    void convertsLeadWithCampaignAndMarksMemberConverted() {
        Campaign campaign = new Campaign();
        campaign.setName("春季活动");
        campaign.setType(CampaignType.EVENT);
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        campaign = campaignRepository.save(campaign);

        Lead lead = new Lead();
        lead.setName("张三");
        lead.setCompany("活动客户");
        lead.setCampaignId(campaign.getId());
        lead = leadRepository.save(lead);

        CampaignMember member = new CampaignMember();
        member.setCampaignId(campaign.getId());
        member.setMemberType(MemberType.LEAD);
        member.setMemberId(lead.getId());
        member.setStatus(MemberStatus.SENT);
        memberRepository.save(member);

        LeadConvertRequest request = new LeadConvertRequest();
        request.setCreateOpportunity(true);
        request.setOpportunityName("活动商机");
        Map<String, Long> result = leadService.convert(lead.getId(), request);

        Opportunity opportunity = opportunityRepository
                .findById(result.get("opportunityId"))
                .orElseThrow(() -> new AssertionError("商机未创建"));
        CampaignMember convertedMember = memberRepository
                .findByCampaignIdAndMemberTypeAndMemberId(
                        campaign.getId(),
                        MemberType.LEAD,
                        lead.getId()
                )
                .orElseThrow(() -> new AssertionError("活动成员不存在"));

        assertEquals(campaign.getId(), opportunity.getCampaignId());
        assertEquals(MemberStatus.CONVERTED, convertedMember.getStatus());
    }

    @Test
    void calculatesRoiAndHandlesMissingActualCost() {
        Campaign campaign = new Campaign();
        campaign.setName("付费活动");
        campaign.setActualCost(new BigDecimal("100"));
        campaign = campaignRepository.save(campaign);

        Opportunity won = new Opportunity();
        won.setName("已赢单商机");
        won.setCampaignId(campaign.getId());
        won.setStage(OpportunityStage.CLOSED_WON);
        won.setAmount(new BigDecimal("300"));
        opportunityRepository.save(won);

        Map<String, Object> stats = campaignService.stats(campaign.getId());

        assertEquals(new BigDecimal("2.0000"), stats.get("roi"));
        assertEquals(1, stats.get("opportunityCount"));

        Campaign freeCampaign = new Campaign();
        freeCampaign.setName("免费活动");
        freeCampaign = campaignRepository.save(freeCampaign);

        Map<String, Object> freeStats = campaignService.stats(freeCampaign.getId());

        assertEquals(BigDecimal.ZERO, freeStats.get("roi"));

        Campaign zeroCostCampaign = new Campaign();
        zeroCostCampaign.setName("零成本活动");
        zeroCostCampaign.setActualCost(BigDecimal.ZERO);
        zeroCostCampaign = campaignRepository.save(zeroCostCampaign);

        Map<String, Object> zeroCostStats = campaignService.stats(
                zeroCostCampaign.getId()
        );

        assertEquals(BigDecimal.ZERO, zeroCostStats.get("roi"));
    }
}
