package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
    boolean existsByCodeIgnoreCase(String code);
}
