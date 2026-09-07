package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.AccountType;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.common.enums.Rating;
import com.mengzhihua.crm.sales.dto.LeadConvertRequest;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeadService {
    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final OpportunityRepository opportunityRepository;

    public LeadService(
            LeadRepository leadRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.opportunityRepository = opportunityRepository;
    }

    public PageResult<Lead> list(
            int page,
            int size,
            String keyword,
            LeadStatus status,
            LeadSource source
    ) {
        Specification<Lead> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), value),
                        builder.like(builder.lower(root.get("company")), value)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (source != null) {
                predicates.add(builder.equal(root.get("source"), source));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Lead> result = leadRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Lead) item);
    }

    public Lead get(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new BizException("线索不存在"));
    }

    public Lead save(Lead lead) {
        if (lead.getStatus() == null) {
            lead.setStatus(LeadStatus.NEW);
        }
        if (lead.getRating() == null) {
            lead.setRating(Rating.WARM);
        }
        return leadRepository.save(lead);
    }

    public void delete(Long id) {
        leadRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Long> convert(Long id, LeadConvertRequest request) {
        Lead lead = get(id);
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new BizException("线索已转化");
        }

        Account account;
        if (request.getAccountId() == null) {
            account = new Account();
            account.setName(
                    lead.getCompany() == null ? lead.getName() : lead.getCompany()
            );
            account.setType(AccountType.PROSPECT);
            account = accountRepository.save(account);
        } else {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new BizException("客户不存在"));
        }

        Contact contact = new Contact();
        contact.setAccountId(account.getId());
        contact.setName(lead.getName());
        contact.setPhone(lead.getPhone());
        contact.setEmail(lead.getEmail());
        contact.setTitle(lead.getTitle());
        contact = contactRepository.save(contact);

        Long opportunityId = null;
        if (Boolean.TRUE.equals(request.getCreateOpportunity())) {
            Opportunity opportunity = new Opportunity();
            opportunity.setAccountId(account.getId());
            opportunity.setName(
                    request.getOpportunityName() == null
                            ? lead.getName() + "商机"
                            : request.getOpportunityName()
            );
            opportunity.setAmount(request.getAmount());
            opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
            opportunity.setStage(OpportunityStage.QUALIFICATION);
            opportunity.setProbability(
                    OpportunityStage.QUALIFICATION.getDefaultProbability()
            );
            opportunityId = opportunityRepository.save(opportunity).getId();
        }

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedAccountId(account.getId());
        lead.setConvertedContactId(contact.getId());
        lead.setConvertedOpportunityId(opportunityId);
        lead.setConvertedAt(LocalDateTime.now());
        leadRepository.save(lead);

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("accountId", account.getId());
        result.put("contactId", contact.getId());
        result.put("opportunityId", opportunityId);
        return result;
    }
}
