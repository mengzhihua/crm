package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long>,
        JpaSpecificationExecutor<Contact> {
    List<Contact> findByAccountId(Long accountId);
}
