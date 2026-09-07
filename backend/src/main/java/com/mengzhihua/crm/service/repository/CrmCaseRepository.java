package com.mengzhihua.crm.service.repository;
import com.mengzhihua.crm.service.entity.CrmCase; import com.mengzhihua.crm.common.Enums;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.*; import java.util.*;
public interface CrmCaseRepository extends JpaRepository<CrmCase,Long> {
 Page<CrmCase> findBySubjectContainingIgnoreCaseOrCaseNoContainingIgnoreCase(String a,String b,Pageable p);
 Page<CrmCase> findByStatus(Enums.CaseStatus s,Pageable p); Page<CrmCase> findByPriority(Enums.CasePriority s,Pageable p); Page<CrmCase> findByAccountId(Long id,Pageable p);
 List<CrmCase> findByAccountId(Long id); long countByStatusNotIn(Collection<Enums.CaseStatus> s); long countBySlaDueAtBeforeAndStatusNotIn(LocalDateTime t,Collection<Enums.CaseStatus> s);
}
