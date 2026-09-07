package com.mengzhihua.crm;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.ContractStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.PaymentMethod;
import com.mengzhihua.crm.common.enums.PaymentStatus;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import com.mengzhihua.crm.contract.dto.PaymentPlanRequest;
import com.mengzhihua.crm.contract.dto.PaymentRecordRequest;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.entity.PaymentPlan;
import com.mengzhihua.crm.contract.repository.ContractRepository;
import com.mengzhihua.crm.contract.repository.PaymentPlanRepository;
import com.mengzhihua.crm.contract.repository.PaymentRecordRepository;
import com.mengzhihua.crm.contract.service.ContractService;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ContractServiceTest {
    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @BeforeEach
    void setUp() {
        paymentRecordRepository.deleteAll();
        paymentPlanRepository.deleteAll();
        contractRepository.deleteAll();
        quoteRepository.deleteAll();
        opportunityRepository.deleteAll();
    }

    @Test
    void rejectsNonAcceptedQuote() {
        Quote quote = createQuote(QuoteStatus.DRAFT, new BigDecimal("1000"));

        assertThrows(
                BizException.class,
                () -> contractService.fromQuote(quote.getId())
        );
    }

    @Test
    void activatesContractAndClosesOpportunity() {
        Opportunity opportunity = createOpportunity();
        Quote quote = createQuote(
                QuoteStatus.ACCEPTED,
                new BigDecimal("1000"),
                opportunity.getId()
        );
        Contract contract = contractService.fromQuote(quote.getId());

        Contract activated = contractService.activate(contract.getId());
        Opportunity savedOpportunity = opportunityRepository
                .findById(opportunity.getId())
                .orElseThrow(() -> new AssertionError("商机不存在"));

        assertEquals(ContractStatus.ACTIVE, activated.getStatus());
        assertEquals(OpportunityStage.CLOSED_WON, savedOpportunity.getStage());
        assertEquals(100, savedOpportunity.getProbability());
        assertNotNull(savedOpportunity.getClosedAt());
    }

    @Test
    void recalculatesPlanAndContractAfterPayments() {
        Quote quote = createQuote(QuoteStatus.ACCEPTED, new BigDecimal("100"));
        Contract contract = contractService.fromQuote(quote.getId());

        PaymentPlanRequest planRequest = new PaymentPlanRequest();
        planRequest.setSeq(1);
        planRequest.setPlanAmount(new BigDecimal("100"));
        planRequest.setPlanDate(LocalDate.now());
        PaymentPlan plan = contractService.addPlan(
                contract.getId(),
                planRequest
        );

        contractService.addPayment(
                contract.getId(),
                paymentRequest(plan.getId(), new BigDecimal("40"))
        );
        PaymentPlan partial = contractService.plans(contract.getId()).get(0);
        Contract afterPartial = contractService.get(contract.getId());

        assertEquals(PaymentStatus.PARTIAL, partial.getStatus());
        assertEquals(
                0,
                afterPartial.getReceivedAmount().compareTo(new BigDecimal("40"))
        );
        assertEquals(
                0,
                afterPartial.getReceivableAmount().compareTo(new BigDecimal("60"))
        );

        contractService.addPayment(
                contract.getId(),
                paymentRequest(plan.getId(), new BigDecimal("60"))
        );
        PaymentPlan paid = contractService.plans(contract.getId()).get(0);
        Contract afterPaid = contractService.get(contract.getId());

        assertEquals(PaymentStatus.PAID, paid.getStatus());
        assertEquals(
                0,
                afterPaid.getReceivedAmount().compareTo(new BigDecimal("100"))
        );
        assertEquals(
                0,
                afterPaid.getReceivableAmount().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void refreshesExpiredActiveContracts() {
        Contract contract = new Contract();
        contract.setName("过期合同");
        contract.setAmount(new BigDecimal("200"));
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setEndDate(LocalDate.now().minusDays(1));
        contract = contractService.save(contract);

        contractService.refreshExpired();

        assertEquals(
                ContractStatus.EXPIRED,
                contractService.get(contract.getId()).getStatus()
        );
    }

    private Opportunity createOpportunity() {
        Opportunity opportunity = new Opportunity();
        opportunity.setName("待签约商机");
        opportunity.setStage(OpportunityStage.NEGOTIATION);
        opportunity.setProbability(75);
        opportunity.setAmount(new BigDecimal("1000"));
        return opportunityRepository.save(opportunity);
    }

    private Quote createQuote(QuoteStatus status, BigDecimal amount) {
        return createQuote(status, amount, null);
    }

    private Quote createQuote(
            QuoteStatus status,
            BigDecimal amount,
            Long opportunityId
    ) {
        Quote quote = new Quote();
        quote.setQuoteNo("QT" + System.nanoTime());
        quote.setName("测试报价");
        quote.setOpportunityId(
                opportunityId == null ? createOpportunity().getId() : opportunityId
        );
        quote.setStatus(status);
        quote.setTotalAmount(amount);
        quote.setSubtotal(amount);
        return quoteRepository.save(quote);
    }

    private PaymentRecordRequest paymentRequest(
            Long planId,
            BigDecimal amount
    ) {
        PaymentRecordRequest request = new PaymentRecordRequest();
        request.setPlanId(planId);
        request.setAmount(amount);
        request.setPaidDate(LocalDate.now());
        request.setMethod(PaymentMethod.BANK_TRANSFER);
        return request;
    }
}
