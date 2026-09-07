package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.AccountLevel;
import com.mengzhihua.crm.common.enums.AccountType;
import com.mengzhihua.crm.common.enums.ArticleStatus;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.Rating;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import com.mengzhihua.crm.service.service.CaseService;
import com.mengzhihua.crm.service.service.KnowledgeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final CaseService caseService;
    private final KnowledgeService knowledgeService;

    public DataInitializer(
            AccountRepository accountRepository,
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            CaseService caseService,
            KnowledgeService knowledgeService
    ) {
        this.accountRepository = accountRepository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseService = caseService;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;
        }
        createAccounts();
        createLeads();
        createOpportunities();
        createCases();
        createKnowledge();
    }

    private void createAccounts() {
        for (int i = 1; i <= 8; i++) {
            Account account = new Account();
            account.setName("演示客户" + i);
            account.setIndustry(i % 2 == 0 ? "制造业" : "互联网");
            account.setType(
                    i % 3 == 0 ? AccountType.CUSTOMER : AccountType.PROSPECT
            );
            account.setLevel(i % 3 == 0 ? AccountLevel.A : AccountLevel.B);
            accountRepository.save(account);
        }
    }

    private void createLeads() {
        for (int i = 1; i <= 12; i++) {
            Lead lead = new Lead();
            lead.setName("演示线索" + i);
            lead.setCompany("潜在企业" + i);
            lead.setSource(LeadSource.values()[i % LeadSource.values().length]);
            lead.setStatus(LeadStatus.values()[i % 4]);
            lead.setRating(Rating.values()[i % Rating.values().length]);
            leadRepository.save(lead);
        }
    }

    private void createOpportunities() {
        for (int i = 1; i <= 10; i++) {
            Opportunity opportunity = new Opportunity();
            opportunity.setName("演示商机" + i);
            opportunity.setAmount(new BigDecimal(i * 10000));
            opportunity.setStage(
                    OpportunityStage.values()[i % OpportunityStage.values().length]
            );
            opportunity.setProbability(
                    opportunity.getStage().getDefaultProbability()
            );
            opportunity.setExpectedCloseDate(LocalDate.now().plusDays(i * 5L));
            opportunityRepository.save(opportunity);
        }
    }

    private void createCases() {
        for (int i = 1; i <= 15; i++) {
            CrmCase crmCase = new CrmCase();
            crmCase.setSubject("演示服务工单" + i);
            crmCase.setPriority(
                    CasePriority.values()[i % CasePriority.values().length]
            );
            crmCase.setStatus(
                    CaseStatus.values()[i % CaseStatus.values().length]
            );
            crmCase.setType(CaseType.PROBLEM);
            if (i <= 2) {
                crmCase.setSlaDueAt(LocalDateTime.now().minusHours(2));
            }
            caseService.save(crmCase);
        }
    }

    private void createKnowledge() {
        for (int i = 1; i <= 6; i++) {
            KnowledgeArticle article = new KnowledgeArticle();
            article.setTitle("客户服务知识" + i);
            article.setCategory("常见问题");
            article.setKeywords("服务,帮助");
            article.setContent("这是演示知识文章内容。");
            article.setStatus(
                    i < 5 ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT
            );
            knowledgeService.save(article);
        }
    }
}
