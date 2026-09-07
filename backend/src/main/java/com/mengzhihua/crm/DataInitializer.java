package com.mengzhihua.crm;

import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.common.enums.AccountLevel;
import com.mengzhihua.crm.common.enums.AccountType;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.ArticleStatus;
import com.mengzhihua.crm.common.enums.CampaignStatus;
import com.mengzhihua.crm.common.enums.CampaignType;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.common.enums.MemberStatus;
import com.mengzhihua.crm.common.enums.MemberType;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.Rating;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.marketing.entity.Campaign;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import com.mengzhihua.crm.marketing.repository.CampaignMemberRepository;
import com.mengzhihua.crm.marketing.repository.CampaignRepository;
import com.mengzhihua.crm.forecast.entity.SalesTarget;
import com.mengzhihua.crm.forecast.repository.SalesTargetRepository;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.entity.PriceBook;
import com.mengzhihua.crm.sales.entity.PriceBookEntry;
import com.mengzhihua.crm.sales.entity.Product;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.repository.PriceBookEntryRepository;
import com.mengzhihua.crm.sales.repository.PriceBookRepository;
import com.mengzhihua.crm.sales.repository.ProductRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import com.mengzhihua.crm.service.entity.AssignmentRule;
import com.mengzhihua.crm.service.entity.SlaPolicy;
import com.mengzhihua.crm.service.repository.AssignmentRuleRepository;
import com.mengzhihua.crm.service.repository.SlaPolicyRepository;
import com.mengzhihua.crm.service.service.CaseService;
import com.mengzhihua.crm.service.service.KnowledgeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final PriceBookRepository priceBookRepository;
    private final PriceBookEntryRepository priceBookEntryRepository;
    private final ApprovalRuleRepository approvalRuleRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository campaignMemberRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final AssignmentRuleRepository assignmentRuleRepository;

    public DataInitializer(
            AccountRepository accountRepository,
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            CaseService caseService,
            KnowledgeService knowledgeService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProductRepository productRepository,
            PriceBookRepository priceBookRepository,
            PriceBookEntryRepository priceBookEntryRepository,
            ApprovalRuleRepository approvalRuleRepository,
            CampaignRepository campaignRepository,
            CampaignMemberRepository campaignMemberRepository,
            SalesTargetRepository salesTargetRepository,
            SlaPolicyRepository slaPolicyRepository,
            AssignmentRuleRepository assignmentRuleRepository
    ) {
        this.accountRepository = accountRepository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseService = caseService;
        this.knowledgeService = knowledgeService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.priceBookRepository = priceBookRepository;
        this.priceBookEntryRepository = priceBookEntryRepository;
        this.approvalRuleRepository = approvalRuleRepository;
        this.campaignRepository = campaignRepository;
        this.campaignMemberRepository = campaignMemberRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.assignmentRuleRepository = assignmentRuleRepository;
    }

    @Override
    public void run(String... args) {
        createUsers();
        createCatalog();
        createApprovalRules();
        if (accountRepository.count() > 0) {
            return;
        }
        createAccounts();
        createLeads();
        createOpportunities();
        createCases();
        createKnowledge();
        createPhase3Data();
    }

    private void createUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        createUser("admin", "admin123", "系统管理员", Role.ADMIN);
        createUser("manager", "123456", "销售经理", Role.SALES_MANAGER);
        createUser("sales", "123456", "销售代表", Role.SALES_REP);
        createUser("service", "123456", "服务专员", Role.SERVICE_AGENT);
    }

    private void createUser(
            String username,
            String password,
            String displayName,
            Role role
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setOwner(username);
        userRepository.save(user);
    }

    private void createCatalog() {
        PriceBook standard = priceBookRepository.findAll().stream()
                .filter(PriceBook::isStandard)
                .findFirst()
                .orElseGet(() -> {
                    PriceBook priceBook = new PriceBook();
                    priceBook.setName("标准价格手册");
                    priceBook.setStandard(true);
                    priceBook.setActive(true);
                    priceBook.setOwner("admin");
                    return priceBookRepository.save(priceBook);
                });
        if (productRepository.count() > 0) {
            return;
        }
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setCode("DEMO-" + i);
            product.setName("演示产品" + i);
            product.setCategory("标准服务");
            product.setUnit("套");
            product.setListPrice(new BigDecimal(i * 10000));
            product.setActive(true);
            product.setOwner("admin");
            product = productRepository.save(product);
            PriceBookEntry entry = new PriceBookEntry();
            entry.setPriceBookId(standard.getId());
            entry.setProductId(product.getId());
            entry.setUnitPrice(product.getListPrice());
            entry.setActive(true);
            entry.setOwner("admin");
            priceBookEntryRepository.save(entry);
        }
    }

    private void createApprovalRules() {
        if (approvalRuleRepository.count() > 0) {
            return;
        }
        createRule(
                "高额报价审批",
                new BigDecimal("500000"),
                null,
                Role.ADMIN,
                1
        );
        createRule(
                "销售经理金额审批",
                new BigDecimal("100000"),
                null,
                Role.SALES_MANAGER,
                2
        );
        createRule(
                "销售经理折扣审批",
                null,
                new BigDecimal("20"),
                Role.SALES_MANAGER,
                3
        );
    }

    private void createRule(
            String name,
            BigDecimal minAmount,
            BigDecimal minDiscountRate,
            Role role,
            int priority
    ) {
        ApprovalRule rule = new ApprovalRule();
        rule.setName(name);
        rule.setTargetType(ApprovalTargetType.QUOTE);
        rule.setMinAmount(minAmount);
        rule.setMinDiscountRate(minDiscountRate);
        rule.setApproverRole(role);
        rule.setPriority(priority);
        rule.setActive(true);
        rule.setOwner("admin");
        approvalRuleRepository.save(rule);
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
            account.setOwner(i % 2 == 0 ? "sales" : "manager");
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
            lead.setOwner(i % 2 == 0 ? "sales" : "manager");
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
            opportunity.setOwner(i % 2 == 0 ? "sales" : "manager");
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
            crmCase.setOwner(i % 2 == 0 ? "service" : "manager");
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
            article.setOwner("service");
            knowledgeService.save(article);
        }
    }

    private void createPhase3Data() {
        createServicePolicies();
        createSalesTargets();
        if (campaignRepository.count() > 0) {
            return;
        }
        Campaign campaign = new Campaign();
        campaign.setName("春季客户增长活动");
        campaign.setType(CampaignType.EVENT);
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        campaign.setStartDate(LocalDate.now().minusDays(5));
        campaign.setEndDate(LocalDate.now().plusDays(25));
        campaign.setBudgetCost(new BigDecimal("10000"));
        campaign.setActualCost(new BigDecimal("3800"));
        campaign.setExpectedRevenue(new BigDecimal("80000"));
        campaign.setOwner("manager");
        campaign = campaignRepository.save(campaign);
        Lead lead = leadRepository.findAll().stream().findFirst().orElse(null);
        if (lead != null) {
            lead.setCampaignId(campaign.getId());
            leadRepository.save(lead);
            CampaignMember member = new CampaignMember();
            member.setCampaignId(campaign.getId());
            member.setMemberId(lead.getId());
            member.setMemberType(MemberType.LEAD);
            member.setStatus(MemberStatus.SENT);
            campaignMemberRepository.save(member);
        }
        Opportunity opportunity = opportunityRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
        if (opportunity != null) {
            opportunity.setCampaignId(campaign.getId());
            opportunityRepository.save(opportunity);
        }
    }

    private void createServicePolicies() {
        if (slaPolicyRepository.count() == 0) {
            createSla(CasePriority.URGENT, 1, 4);
            createSla(CasePriority.HIGH, 2, 8);
            createSla(CasePriority.MEDIUM, 4, 24);
            createSla(CasePriority.LOW, 12, 72);
        }
        if (assignmentRuleRepository.count() == 0) {
            AssignmentRule rule = new AssignmentRule();
            rule.setName("默认服务专员");
            rule.setPriority(1);
            rule.setAssignTo("service");
            rule.setActive(true);
            rule.setOwner("admin");
            assignmentRuleRepository.save(rule);
        }
    }

    private void createSla(
            CasePriority priority,
            int responseHours,
            int resolveHours
    ) {
        SlaPolicy policy = new SlaPolicy();
        policy.setPriority(priority);
        policy.setResponseHours(responseHours);
        policy.setResolveHours(resolveHours);
        policy.setActive(true);
        policy.setOwner("admin");
        slaPolicyRepository.save(policy);
    }

    private void createSalesTargets() {
        if (salesTargetRepository.count() > 0) {
            return;
        }
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        createTarget("sales", year, month, new BigDecimal("200000"));
        createTarget("manager", year, month, new BigDecimal("500000"));
    }

    private void createTarget(
            String owner,
            int year,
            int month,
            BigDecimal amount
    ) {
        SalesTarget target = new SalesTarget();
        target.setOwner(owner);
        target.setYear(year);
        target.setMonth(month);
        target.setTargetAmount(amount);
        salesTargetRepository.save(target);
    }
}
