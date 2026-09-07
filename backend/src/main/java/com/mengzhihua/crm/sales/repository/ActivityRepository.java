package com.mengzhihua.crm.sales.repository;
import com.mengzhihua.crm.sales.entity.Activity; import com.mengzhihua.crm.common.Enums;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ActivityRepository extends JpaRepository<Activity,Long> { Page<Activity> findBySubjectContainingIgnoreCase(String keyword,Pageable p); Page<Activity> findByRelatedTypeAndRelatedId(Enums.RelatedType t,Long id,Pageable p); Page<Activity> findByStatus(Enums.ActivityStatus s,Pageable p); List<Activity> findTop10ByOrderByDueTimeDesc(); }
