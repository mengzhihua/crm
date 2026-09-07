package com.mengzhihua.crm.forecast.repository;

import com.mengzhihua.crm.forecast.entity.SalesTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long>,
        JpaSpecificationExecutor<SalesTarget> {
    Optional<SalesTarget> findByOwnerAndYearAndMonth(
            String owner,
            Integer year,
            Integer month
    );

    List<SalesTarget> findByYearAndMonth(Integer year, Integer month);

    List<SalesTarget> findByYear(Integer year);
}
