package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {
    boolean existsByNameIgnoreCase(String name);
}
