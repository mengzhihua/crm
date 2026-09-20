package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.service.CaseService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 已有库也会补上控制塔演示商机 / 工单，避免 DataInitializer 只在空库执行。 */
@Component
@Profile("!test")
@Order(20)
public class IrDemoSeeder implements ApplicationRunner {
    private final OpportunityRepository opportunities;
    private final CrmCaseRepository cases;
    private final CaseService caseService;

    public IrDemoSeeder(
            OpportunityRepository opportunities,
            CrmCaseRepository cases,
            CaseService caseService) {
        this.opportunities = opportunities;
        this.cases = cases;
        this.caseService = caseService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (opportunities.findAll().stream().noneMatch(item -> "IR-OPP-QUAL".equals(item.getName()))) {
            Opportunity opportunity = new Opportunity();
            opportunity.setName("IR-OPP-QUAL");
            opportunity.setAmount(new BigDecimal("2400000"));
            opportunity.setStage(OpportunityStage.QUALIFICATION);
            opportunity.setProbability(OpportunityStage.QUALIFICATION.getDefaultProbability());
            opportunity.setExpectedCloseDate(LocalDate.now().plusDays(21));
            opportunity.setDescription("IR 控制塔演示：资格评估阶段商机");
            opportunities.save(opportunity);
        }
        if (cases.findAll().stream().noneMatch(item -> "CS-IR-NEW".equals(item.getCaseNo()))) {
            CrmCase crmCase = new CrmCase();
            crmCase.setCaseNo("CS-IR-NEW");
            crmCase.setSubject("交期投诉");
            crmCase.setStatus(CaseStatus.NEW);
            crmCase.setPriority(CasePriority.HIGH);
            crmCase.setType(CaseType.PROBLEM);
            caseService.save(crmCase);
        }
    }
}
