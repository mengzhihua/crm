package com.mengzhihua.crm.sales.repository;
import com.mengzhihua.crm.sales.entity.Lead;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface LeadRepository extends JpaRepository<Lead,Long> {
    Page<Lead> findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(String name,String company,Pageable p);
    long countByStatus(com.mengzhihua.crm.common.Enums.LeadStatus s);
}
