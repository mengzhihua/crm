package com.mengzhihua.crm.sales.repository;

import com.mengzhihua.crm.sales.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long>,
        JpaSpecificationExecutor<Activity> {
    List<Activity> findTop10ByOrderByDueTimeDesc();
}
