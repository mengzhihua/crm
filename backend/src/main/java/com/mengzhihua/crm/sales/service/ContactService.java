package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.permission.service.DataScopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {
    private final ContactRepository contactRepository;
    private final DataScopeService dataScopeService;

    public ContactService(
            ContactRepository contactRepository,
            DataScopeService dataScopeService
    ) {
        this.contactRepository = contactRepository;
        this.dataScopeService = dataScopeService;
    }

    public PageResult<Contact> list(
            int page,
            int size,
            String keyword,
            Long accountId
    ) {
        Specification<Contact> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            if (accountId != null) {
                predicates.add(builder.equal(root.get("accountId"), accountId));
            }
            return builder.and(
                    builder.and(predicates.toArray(new Predicate[0])),
                    dataScopeService.<Contact>scope(DataObjectType.CONTACT)
                            .toPredicate(root, query, builder)
            );
        };
        Page<Contact> result = contactRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Contact) item);
    }

    public Contact get(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BizException("联系人不存在"));
        dataScopeService.checkRead(DataObjectType.CONTACT, contact.getOwner(), id);
        return contact;
    }

    public Contact getForEdit(Long id) {
        Contact contact = get(id);
        dataScopeService.checkEdit(DataObjectType.CONTACT, contact.getOwner(), id);
        return contact;
    }

    public Contact save(Contact contact) {
        if (contact.getId() != null) {
            Contact current = getForEdit(contact.getId());
            contact.setOwner(current.getOwner());
        }
        return contactRepository.save(contact);
    }

    public void delete(Long id) {
        Contact contact = getForEdit(id);
        contactRepository.deleteById(id);
    }
}
