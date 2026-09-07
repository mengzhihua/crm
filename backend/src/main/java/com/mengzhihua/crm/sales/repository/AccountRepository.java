package com.mengzhihua.crm.sales.repository;
import com.mengzhihua.crm.sales.entity.Account;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account,Long> { Page<Account> findByNameContainingIgnoreCase(String keyword,Pageable p); boolean existsByNameIgnoreCase(String name); }
