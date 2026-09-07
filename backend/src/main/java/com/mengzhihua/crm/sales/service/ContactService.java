package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.repository.ContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
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
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Contact> result = contactRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Contact) item);
    }

    public Contact get(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new BizException("联系人不存在"));
    }

    public Contact save(Contact contact) {
        return contactRepository.save(contact);
    }

    public void delete(Long id) {
        contactRepository.deleteById(id);
    }
}
