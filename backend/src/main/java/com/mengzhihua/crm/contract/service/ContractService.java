package com.mengzhihua.crm.contract.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.SerialNumberGenerator;
import com.mengzhihua.crm.common.enums.ContractStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.PaymentStatus;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.contract.dto.ContractTerminateRequest;
import com.mengzhihua.crm.contract.dto.PaymentPlanRequest;
import com.mengzhihua.crm.contract.dto.PaymentRecordRequest;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.entity.PaymentPlan;
import com.mengzhihua.crm.contract.entity.PaymentRecord;
import com.mengzhihua.crm.contract.repository.ContractRepository;
import com.mengzhihua.crm.contract.repository.PaymentPlanRepository;
import com.mengzhihua.crm.contract.repository.PaymentRecordRepository;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.entity.Quote;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.sales.repository.QuoteRepository;
import com.mengzhihua.crm.record.service.FieldHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final QuoteRepository quoteRepository;
    private final OpportunityRepository opportunityRepository;
    private final FieldHistoryService fieldHistoryService;

    public ContractService(
            ContractRepository contractRepository,
            PaymentPlanRepository paymentPlanRepository,
            PaymentRecordRepository paymentRecordRepository,
            QuoteRepository quoteRepository,
            OpportunityRepository opportunityRepository,
            FieldHistoryService fieldHistoryService
    ) {
        this.contractRepository = contractRepository;
        this.paymentPlanRepository = paymentPlanRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.quoteRepository = quoteRepository;
        this.opportunityRepository = opportunityRepository;
        this.fieldHistoryService = fieldHistoryService;
    }

    public PageResult<Contract> list(
            int page,
            int size,
            String keyword,
            ContractStatus status,
            Long accountId
    ) {
        Specification<Contract> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("contractNo")), value),
                        builder.like(builder.lower(root.get("name")), value)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (accountId != null) {
                predicates.add(builder.equal(root.get("accountId"), accountId));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Contract> result = contractRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Contract) item);
    }

    public Contract get(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new BizException("合同不存在"));
    }

    public void delete(Long id) {
        get(id);
        contractRepository.deleteById(id);
    }

    public Contract save(Contract contract) {
        if (contract.getId() == null) {
            contract.setContractNo(nextNo());
            if (contract.getStatus() == null) {
                contract.setStatus(ContractStatus.DRAFT);
            }
        } else {
            Contract current = get(contract.getId());
            contract.setContractNo(current.getContractNo());
        }
        if (contract.getAmount() == null) {
            contract.setAmount(BigDecimal.ZERO);
        }
        if (contract.getId() == null) {
            Contract saved = contractRepository.save(contract);
            recalculateContract(saved);
            return contractRepository.save(saved);
        }
        recalculateContract(contract);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract fromQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new BizException("报价单不存在"));
        if (quote.getStatus() != com.mengzhihua.crm.common.enums.QuoteStatus.ACCEPTED) {
            throw new BizException("只有已接受报价可以生成合同");
        }
        Contract contract = new Contract();
        contract.setContractNo(nextNo());
        contract.setName(quote.getName() + "合同");
        contract.setAccountId(quote.getAccountId());
        contract.setOpportunityId(quote.getOpportunityId());
        contract.setQuoteId(quote.getId());
        contract.setAmount(quote.getTotalAmount());
        contract.setReceivedAmount(BigDecimal.ZERO);
        contract.setReceivableAmount(quote.getTotalAmount());
        contract.setStatus(ContractStatus.PENDING_SIGN);
        contract.setSignDate(LocalDate.now());
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract activate(Long id) {
        Contract contract = get(id);
        ContractStatus oldStatus = contract.getStatus();
        if (contract.getStatus() != ContractStatus.PENDING_SIGN) {
            throw new BizException("只有待签署合同可以激活");
        }
        contract.setStatus(ContractStatus.ACTIVE);
        if (contract.getStartDate() == null) {
            contract.setStartDate(LocalDate.now());
        }
        if (contract.getOpportunityId() != null) {
            Opportunity opportunity = opportunityRepository.findById(
                    contract.getOpportunityId()
            ).orElseThrow(() -> new BizException("商机不存在"));
            opportunity.setStage(OpportunityStage.CLOSED_WON);
            opportunity.setProbability(100);
            opportunity.setClosedAt(java.time.LocalDateTime.now());
            opportunityRepository.save(opportunity);
        }
        Contract saved = contractRepository.save(contract);
        fieldHistoryService.record(
                RelatedType.CONTRACT,
                id,
                "status",
                oldStatus,
                saved.getStatus()
        );
        return saved;
    }

    public Contract terminate(Long id, ContractTerminateRequest request) {
        Contract contract = get(id);
        ContractStatus oldStatus = contract.getStatus();
        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BizException("合同已终止");
        }
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setRemark(request.getReason());
        Contract saved = contractRepository.save(contract);
        fieldHistoryService.record(
                RelatedType.CONTRACT,
                id,
                "status",
                oldStatus,
                saved.getStatus()
        );
        return saved;
    }

    public List<PaymentPlan> plans(Long contractId) {
        get(contractId);
        List<PaymentPlan> plans = paymentPlanRepository
                .findByContractIdOrderBySeqAsc(contractId);
        for (PaymentPlan plan : plans) {
            refreshPlanStatus(plan);
        }
        return paymentPlanRepository.saveAll(plans);
    }

    public PaymentPlan addPlan(Long contractId, PaymentPlanRequest request) {
        get(contractId);
        PaymentPlan plan = new PaymentPlan();
        plan.setContractId(contractId);
        plan.setSeq(request.getSeq());
        plan.setPlanAmount(request.getPlanAmount());
        plan.setPlanDate(request.getPlanDate());
        plan.setStatus(PaymentStatus.UNPAID);
        plan.setPaidAmount(BigDecimal.ZERO);
        return paymentPlanRepository.save(plan);
    }

    @Transactional
    public PaymentRecord addPayment(
            Long contractId,
            PaymentRecordRequest request
    ) {
        Contract contract = get(contractId);
        if (request.getPlanId() != null) {
            PaymentPlan plan = paymentPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new BizException("回款计划不存在"));
            if (!contractId.equals(plan.getContractId())) {
                throw new BizException("回款计划不属于当前合同");
            }
        }
        PaymentRecord record = new PaymentRecord();
        record.setContractId(contractId);
        record.setPlanId(request.getPlanId());
        record.setAmount(request.getAmount());
        record.setPaidDate(request.getPaidDate());
        record.setMethod(request.getMethod());
        record.setVoucherNo(request.getVoucherNo());
        record.setRemark(request.getRemark());
        record = paymentRecordRepository.save(record);
        if (request.getPlanId() != null) {
            PaymentPlan plan = paymentPlanRepository.findById(request.getPlanId())
                    .get();
            refreshPlanStatus(plan);
            paymentPlanRepository.save(plan);
        }
        recalculateContract(contract);
        return paymentRecordRepository.save(record);
    }

    public List<PaymentRecord> payments(Long contractId) {
        get(contractId);
        return paymentRecordRepository.findByContractIdOrderByPaidDateDesc(
                contractId
        );
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshExpired() {
        for (Contract contract : contractRepository.findByStatus(
                ContractStatus.ACTIVE
        )) {
            if (contract.getEndDate() != null
                    && contract.getEndDate().isBefore(LocalDate.now())) {
                contract.setStatus(ContractStatus.EXPIRED);
                contractRepository.save(contract);
            }
        }
    }

    private void refreshPlanStatus(PaymentPlan plan) {
        BigDecimal paid = paymentRecordRepository.findByPlanId(plan.getId())
                .stream()
                .map(PaymentRecord::getAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        plan.setPaidAmount(paid);
        if (paid.compareTo(plan.getPlanAmount()) >= 0) {
            plan.setStatus(PaymentStatus.PAID);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            plan.setStatus(PaymentStatus.PARTIAL);
        } else if (plan.getPlanDate() != null
                && plan.getPlanDate().isBefore(LocalDate.now())) {
            plan.setStatus(PaymentStatus.OVERDUE);
        } else {
            plan.setStatus(PaymentStatus.UNPAID);
        }
    }

    private void recalculateContract(Contract contract) {
        BigDecimal received = paymentRecordRepository
                .findByContractIdOrderByPaidDateDesc(contract.getId())
                .stream()
                .map(PaymentRecord::getAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        contract.setReceivedAmount(received);
        BigDecimal amount = contract.getAmount() == null
                ? BigDecimal.ZERO
                : contract.getAmount();
        contract.setReceivableAmount(amount.subtract(received).max(BigDecimal.ZERO));
    }

    private String nextNo() {
        String prefix = "HT" + LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
        Contract latest = contractRepository
                .findTopByContractNoStartingWithOrderByContractNoDesc(prefix)
                .orElse(null);
        return SerialNumberGenerator.next(
                prefix,
                latest == null ? null : latest.getContractNo()
        );
    }
}
