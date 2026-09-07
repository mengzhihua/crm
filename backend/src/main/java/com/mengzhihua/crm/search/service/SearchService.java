package com.mengzhihua.crm.search.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.repository.ContractRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.entity.KnowledgeArticle;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.repository.KnowledgeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final CrmCaseRepository caseRepository;
    private final ContractRepository contractRepository;
    private final KnowledgeRepository knowledgeRepository;

    public SearchService(
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository,
            CrmCaseRepository caseRepository,
            ContractRepository contractRepository,
            KnowledgeRepository knowledgeRepository
    ) {
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseRepository = caseRepository;
        this.contractRepository = contractRepository;
        this.knowledgeRepository = knowledgeRepository;
    }

    public Map<String, List<Map<String, Object>>> search(String query) {
        String value = query == null ? "" : query.trim().toLowerCase();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("客户", accounts(value));
        result.put("联系人", contacts(value));
        if (CurrentUser.role() != Role.SERVICE_AGENT) {
            result.put("线索", leads(value));
            result.put("商机", opportunities(value));
            result.put("合同", contracts(value));
        }
        result.put("工单", cases(value));
        result.put("知识库", knowledge(value));
        return result;
    }

    private List<Map<String, Object>> accounts(String value) {
        Specification<Account> spec = (root, query, builder) ->
                builder.like(builder.lower(root.get("name")), "%" + value + "%");
        List<Map<String, Object>> result = new ArrayList<>();
        accountRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item("ACCOUNT", item.getId(), item.getName(), item.getIndustry()))
        );
        return result;
    }

    private List<Map<String, Object>> contacts(String value) {
        Specification<Contact> spec = (root, query, builder) -> {
            String pattern = "%" + value + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("phone")), pattern)
            );
        };
        List<Map<String, Object>> result = new ArrayList<>();
        contactRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item("CONTACT", item.getId(), item.getName(), item.getPhone()))
        );
        return result;
    }

    private List<Map<String, Object>> leads(String value) {
        Specification<Lead> spec = (root, query, builder) -> {
            String pattern = "%" + value + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("company")), pattern)
            );
        };
        List<Map<String, Object>> result = new ArrayList<>();
        leadRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item("LEAD", item.getId(), item.getName(), item.getCompany()))
        );
        return result;
    }

    private List<Map<String, Object>> opportunities(String value) {
        Specification<Opportunity> spec = (root, query, builder) ->
                builder.like(builder.lower(root.get("name")), "%" + value + "%");
        List<Map<String, Object>> result = new ArrayList<>();
        opportunityRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item(
                        "OPPORTUNITY",
                        item.getId(),
                        item.getName(),
                        String.valueOf(item.getAmount())
                ))
        );
        return result;
    }

    private List<Map<String, Object>> cases(String value) {
        Specification<CrmCase> spec = (root, query, builder) -> {
            String pattern = "%" + value + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("caseNo")), pattern),
                    builder.like(builder.lower(root.get("subject")), pattern)
            );
        };
        List<Map<String, Object>> result = new ArrayList<>();
        caseRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item("CASE", item.getId(), item.getCaseNo(), item.getSubject()))
        );
        return result;
    }

    private List<Map<String, Object>> contracts(String value) {
        Specification<Contract> spec = (root, query, builder) -> {
            String pattern = "%" + value + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("contractNo")), pattern),
                    builder.like(builder.lower(root.get("name")), pattern)
            );
        };
        List<Map<String, Object>> result = new ArrayList<>();
        contractRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item(
                        "CONTRACT",
                        item.getId(),
                        item.getContractNo(),
                        item.getName()
                ))
        );
        return result;
    }

    private List<Map<String, Object>> knowledge(String value) {
        Specification<KnowledgeArticle> spec = (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + value + "%");
        List<Map<String, Object>> result = new ArrayList<>();
        knowledgeRepository.findAll(spec, PageRequest.of(0, 5)).forEach(item ->
                result.add(item("KNOWLEDGE", item.getId(), item.getTitle(), item.getCategory()))
        );
        return result;
    }

    private Map<String, Object> item(
            String type,
            Long id,
            String title,
            String subtitle
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("id", id);
        result.put("title", title);
        result.put("subtitle", subtitle);
        return result;
    }
}
