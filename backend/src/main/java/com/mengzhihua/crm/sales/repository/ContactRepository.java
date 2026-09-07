package com.mengzhihua.crm.sales.repository;
import com.mengzhihua.crm.sales.entity.Contact;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ContactRepository extends JpaRepository<Contact,Long> { Page<Contact> findByNameContainingIgnoreCase(String keyword,Pageable p); Page<Contact> findByAccountId(Long accountId,Pageable p); List<Contact> findByAccountId(Long accountId); }
