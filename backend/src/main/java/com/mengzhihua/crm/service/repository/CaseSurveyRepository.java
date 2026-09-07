package com.mengzhihua.crm.service.repository;

import com.mengzhihua.crm.service.entity.CaseSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseSurveyRepository extends JpaRepository<CaseSurvey, Long> {
    Optional<CaseSurvey> findByCaseId(Long caseId);

    List<CaseSurvey> findAllByScoreIsNotNull();
}
