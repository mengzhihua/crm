package com.mengzhihua.crm;

import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.entity.ApprovalRule;
import com.mengzhihua.crm.approval.repository.ApprovalRequestRepository;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.approval.service.ApprovalService;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MultiStepApprovalTest {
    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalRuleRepository ruleRepository;

    @Autowired
    private ApprovalRequestRepository requestRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        ruleRepository.deleteAll();
        quoteRepository.deleteAll();
        ApprovalRule rule = new ApprovalRule();
        rule.setName("两级审批");
        rule.setTargetType(ApprovalTargetType.QUOTE);
        rule.setMinAmount(new BigDecimal("100"));
        rule.setApproverRoles("SALES_MANAGER,ADMIN");
        rule.setPriority(1);
        rule.setActive(true);
        ruleRepository.save(rule);
    }

    @Test
    @WithMockUser(username = "sales", roles = "SALES_REP")
    void advancesThenCompletesApproval() {
        Quote quote = new Quote();
        quote.setName("测试报价");
        quote.setQuoteNo("QT-TEST-1");
        quote.setOpportunityId(1L);
        quote.setStatus(QuoteStatus.IN_REVIEW);
        quote.setTotalAmount(new BigDecimal("200"));
        quote = quoteRepository.save(quote);
        approvalService.submit(
                ApprovalTargetType.QUOTE,
                quote.getId(),
                new BigDecimal("200"),
                BigDecimal.ZERO,
                "测试报价"
        );
        ApprovalRequest request = requestRepository.findAll().get(0);
        ApprovalRequest first = approveAsManager(request.getId());
        assertEquals(ApprovalStatus.PENDING, first.getStatus());
        assertEquals(Role.ADMIN, first.getApproverRole());
        assertEquals(
                QuoteStatus.IN_REVIEW,
                quoteRepository.findById(quote.getId()).get().getStatus()
        );
        ApprovalRequest second = approveAsAdmin(request.getId());
        assertEquals(ApprovalStatus.APPROVED, second.getStatus());
        assertEquals(
                QuoteStatus.APPROVED,
                quoteRepository.findById(quote.getId()).get().getStatus()
        );
    }

    private ApprovalRequest approveAsManager(Long id) {
        org.springframework.security.core.context.SecurityContextHolder
                .getContext().setAuthentication(
                        new org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken(
                                "manager",
                                null,
                                java.util.Collections.singletonList(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority(
                                                "ROLE_SALES_MANAGER"
                                        )
                                )
                        )
                );
        return approvalService.decide(id, true, null);
    }

    private ApprovalRequest approveAsAdmin(Long id) {
        org.springframework.security.core.context.SecurityContextHolder
                .getContext().setAuthentication(
                        new org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken(
                                "admin",
                                null,
                                java.util.Collections.singletonList(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
                );
        return approvalService.decide(id, true, null);
    }
}
