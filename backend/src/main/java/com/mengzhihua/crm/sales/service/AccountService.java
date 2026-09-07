package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final OpportunityRepository opportunityRepository;
    private final CrmCaseRepository caseRepository;

    public AccountService(
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            OpportunityRepository opportunityRepository,
            CrmCaseRepository caseRepository
    ) {
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseRepository = caseRepository;
    }

    public PageResult<Account> list(int page, int size, String keyword) {
        Specification<Account> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Account> result = accountRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Account) item);
    }

    public Account get(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BizException("客户不存在"));
    }

    public Account save(Account account) {
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            throw new BizException("客户名称不能为空");
        }
        if (account.getId() == null
                && accountRepository.existsByNameIgnoreCase(account.getName())) {
            throw new BizException("客户名称已存在");
        }
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

    public Map<String, Object> overview(Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("account", get(id));
        result.put("contacts", contactRepository.findByAccountId(id));
        result.put("opportunities", opportunityRepository.findByAccountId(id));
        result.put("cases", caseRepository.findByAccountId(id));
        return result;
    }
}
