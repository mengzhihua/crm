package com.mengzhihua.crm.permission.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.sales.entity.Account;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.entity.Lead;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.AccountRepository;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.sales.repository.LeadRepository;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.AccessLevel;
import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.common.enums.DataScope;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.permission.entity.RecordShare;
import com.mengzhihua.crm.permission.entity.RoleDataScope;
import com.mengzhihua.crm.permission.repository.RecordShareRepository;
import com.mengzhihua.crm.permission.repository.RoleDataScopeRepository;
import com.mengzhihua.crm.contract.entity.Contract;
import com.mengzhihua.crm.contract.repository.ContractRepository;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataScopeService {
    private final RoleDataScopeRepository scopeRepository;
    private final RecordShareRepository shareRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final OpportunityRepository opportunityRepository;
    private final CrmCaseRepository caseRepository;
    private final ContractRepository contractRepository;

    public DataScopeService(
            RoleDataScopeRepository scopeRepository,
            RecordShareRepository shareRepository,
            UserRepository userRepository,
            LeadRepository leadRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            OpportunityRepository opportunityRepository,
            CrmCaseRepository caseRepository,
            ContractRepository contractRepository
    ) {
        this.scopeRepository = scopeRepository;
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.opportunityRepository = opportunityRepository;
        this.caseRepository = caseRepository;
        this.contractRepository = contractRepository;
    }

    public <T> Specification<T> scope(DataObjectType type) {
        String username = CurrentUser.username();
        if (username == null || scopeFor(type) == DataScope.ALL) {
            return (root, query, builder) -> builder.conjunction();
        }
        List<String> owners = allowedOwners(type, username);
        List<Long> shared = sharedIds(type, username, AccessLevel.READ);
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!owners.isEmpty()) {
                predicates.add(root.get("owner").in(owners));
            }
            if (!shared.isEmpty()) {
                predicates.add(root.get("id").in(shared));
            }
            return predicates.isEmpty()
                    ? builder.disjunction()
                    : builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public void checkRead(DataObjectType type, String owner, Long recordId) {
        check(type, owner, recordId, AccessLevel.READ);
    }

    public void checkEdit(DataObjectType type, String owner, Long recordId) {
        check(type, owner, recordId, AccessLevel.EDIT);
    }

    public String ownerOf(DataObjectType type, Long recordId) {
        switch (type) {
            case LEAD:
                return leadRepository.findById(recordId)
                        .map(Lead::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            case ACCOUNT:
                return accountRepository.findById(recordId)
                        .map(Account::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            case CONTACT:
                return contactRepository.findById(recordId)
                        .map(Contact::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            case OPPORTUNITY:
                return opportunityRepository.findById(recordId)
                        .map(Opportunity::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            case CASE:
                return caseRepository.findById(recordId)
                        .map(CrmCase::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            case CONTRACT:
                return contractRepository.findById(recordId)
                        .map(Contract::getOwner)
                        .orElseThrow(() -> new BizException("记录不存在"));
            default:
                throw new BizException("记录不存在");
        }
    }

    private void check(
            DataObjectType type,
            String owner,
            Long recordId,
            AccessLevel accessLevel
    ) {
        String username = CurrentUser.username();
        DataScope scope = scopeFor(type);
        if (username == null || scope == DataScope.ALL
                || username.equals(owner)
                || (scope == DataScope.TEAM && teamOwners(type, username).contains(owner))
                || hasShare(type, recordId, username, accessLevel)) {
            return;
        }
        throw new BizException("无权访问该记录");
    }

    private DataScope scopeFor(DataObjectType type) {
        Role role = CurrentUser.role();
        if (role == null) {
            return DataScope.ALL;
        }
        return scopeRepository.findByRoleAndObjectType(role, type)
                .map(RoleDataScope::getScope)
                .orElse(DataScope.ALL);
    }

    private List<String> allowedOwners(DataObjectType type, String username) {
        DataScope scope = scopeFor(type);
        if (scope == DataScope.OWN) {
            return java.util.Collections.singletonList(username);
        }
        return teamOwners(type, username);
    }

    private List<String> teamOwners(DataObjectType type, String username) {
        User current = userRepository.findByUsername(username).orElse(null);
        if (current == null || current.getTeam() == null) {
            return java.util.Collections.singletonList(username);
        }
        return userRepository.findAll().stream()
                .filter(user -> current.getTeam().equals(user.getTeam()))
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    private List<Long> sharedIds(
            DataObjectType type,
            String username,
            AccessLevel accessLevel
    ) {
        return shareRepository.findAll().stream()
                .filter(share -> share.getObjectType() == type)
                .filter(share -> username.equals(share.getSharedWith()))
                .filter(share -> accessLevel == AccessLevel.READ
                        || share.getAccessLevel() == AccessLevel.EDIT)
                .map(RecordShare::getRecordId)
                .collect(Collectors.toList());
    }

    private boolean hasShare(
            DataObjectType type,
            Long recordId,
            String username,
            AccessLevel accessLevel
    ) {
        return shareRepository
                .findByObjectTypeAndRecordIdAndSharedWith(type, recordId, username)
                .filter(share -> accessLevel == AccessLevel.READ
                        || share.getAccessLevel() == AccessLevel.EDIT)
                .isPresent();
    }
}
