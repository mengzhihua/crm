package com.mengzhihua.crm.sales.repository;
import com.mengzhihua.crm.sales.entity.Opportunity; import com.mengzhihua.crm.common.Enums;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OpportunityRepository extends JpaRepository<Opportunity,Long> {
 Page<Opportunity> findByNameContainingIgnoreCase(String keyword,Pageable p); List<Opportunity> findByAccountId(Long accountId); List<Opportunity> findByStage(Enums.OpportunityStage stage);
 long countByStageNotIn(Collection<Enums.OpportunityStage> stages); List<Opportunity> findByStageNotIn(Collection<Enums.OpportunityStage> stages);
}
