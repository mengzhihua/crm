package com.mengzhihua.crm;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.common.enums.CaseType;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.dto.CaseSurveyRequest;
import com.mengzhihua.crm.service.entity.AssignmentRule;
import com.mengzhihua.crm.service.entity.CaseComment;
import com.mengzhihua.crm.service.entity.CaseSurvey;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.entity.SlaPolicy;
import com.mengzhihua.crm.service.repository.AssignmentRuleRepository;
import com.mengzhihua.crm.service.repository.CaseCommentRepository;
import com.mengzhihua.crm.service.repository.CaseSurveyRepository;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.repository.SlaPolicyRepository;
import com.mengzhihua.crm.service.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CaseRuleTest {
    @Autowired
    private CaseService caseService;

    @Autowired
    private CrmCaseRepository caseRepository;

    @Autowired
    private AssignmentRuleRepository assignmentRuleRepository;

    @Autowired
    private SlaPolicyRepository slaPolicyRepository;

    @Autowired
    private CaseCommentRepository commentRepository;

    @Autowired
    private CaseSurveyRepository surveyRepository;

    @BeforeEach
    void setUp() {
        surveyRepository.deleteAll();
        commentRepository.deleteAll();
        caseRepository.deleteAll();
        assignmentRuleRepository.deleteAll();
        slaPolicyRepository.deleteAll();
    }

    @Test
    void usesFirstMatchingAssignmentRuleAndFallsBackToService() {
        AssignmentRule first = new AssignmentRule();
        first.setName("高优先级规则");
        first.setPriority(1);
        first.setActive(true);
        first.setCasePriority(CasePriority.HIGH);
        first.setAssignTo("manager");
        assignmentRuleRepository.save(first);

        AssignmentRule second = new AssignmentRule();
        second.setName("通用规则");
        second.setPriority(2);
        second.setActive(true);
        second.setAssignTo("sales");
        assignmentRuleRepository.save(second);

        CrmCase matched = new CrmCase();
        matched.setPriority(CasePriority.HIGH);
        matched.setType(CaseType.PROBLEM);
        matched = caseService.save(matched);

        assertEquals("manager", matched.getOwner());

        assignmentRuleRepository.deleteAll();
        CrmCase fallback = caseService.save(new CrmCase());

        assertEquals("service", fallback.getOwner());
    }

    @Test
    void usesConfiguredResolveHoursForNewCases() {
        SlaPolicy policy = new SlaPolicy();
        policy.setPriority(CasePriority.MEDIUM);
        policy.setResponseHours(1);
        policy.setResolveHours(2);
        policy.setActive(true);
        slaPolicyRepository.save(policy);

        LocalDateTime before = LocalDateTime.now().plusHours(2);
        CrmCase crmCase = new CrmCase();
        crmCase.setPriority(CasePriority.MEDIUM);
        CrmCase saved = caseService.save(crmCase);
        LocalDateTime after = LocalDateTime.now().plusHours(2);

        assertTrue(!saved.getSlaDueAt().isBefore(before));
        assertTrue(!saved.getSlaDueAt().isAfter(after));
    }

    @Test
    void escalatesOverdueCaseAndAddsInternalComment() {
        CrmCase crmCase = new CrmCase();
        crmCase.setStatus(CaseStatus.IN_PROGRESS);
        crmCase.setPriority(CasePriority.HIGH);
        crmCase.setSlaDueAt(LocalDateTime.now().minusMinutes(1));
        crmCase = caseService.save(crmCase);

        caseService.checkSla();

        CrmCase escalated = caseService.get(crmCase.getId());
        CaseComment comment = commentRepository
                .findByCaseIdOrderByCreatedAtAsc(crmCase.getId())
                .get(0);

        assertEquals(CaseStatus.ESCALATED, escalated.getStatus());
        assertEquals(CasePriority.URGENT, escalated.getPriority());
        assertEquals(true, escalated.getEscalated());
        assertEquals("SLA 超期自动升级", comment.getContent());
        assertEquals(true, comment.getInternal());
    }

    @Test
    void onlyResolvedCaseCanBeSurveyedOnce() {
        CrmCase inProgress = new CrmCase();
        inProgress.setStatus(CaseStatus.IN_PROGRESS);
        inProgress = caseService.save(inProgress);
        Long caseId = inProgress.getId();

        CaseSurveyRequest request = new CaseSurveyRequest();
        request.setScore(5);
        request.setComment("处理及时");

        assertThrows(
                BizException.class,
                () -> caseService.survey(caseId, request)
        );

        CaseStatusRequest resolveRequest = new CaseStatusRequest();
        resolveRequest.setStatus(CaseStatus.RESOLVED);
        resolveRequest.setSolution("问题已解决");
        caseService.changeStatus(caseId, resolveRequest);

        CaseSurvey survey = caseService.survey(caseId, request);
        CrmCase resolved = caseService.get(caseId);

        assertEquals(5, survey.getScore());
        assertEquals(5, resolved.getSatisfactionScore());
        assertNotNull(survey.getCreatedAt());
        assertTrue(
                ChronoUnit.SECONDS.between(
                        survey.getCreatedAt(),
                        LocalDateTime.now()
                ) < 5
        );
        assertThrows(
                BizException.class,
                () -> caseService.survey(caseId, request)
        );
    }
}
