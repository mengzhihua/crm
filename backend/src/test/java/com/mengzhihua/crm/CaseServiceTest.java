package com.mengzhihua.crm;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.CasePriority;
import com.mengzhihua.crm.common.enums.CaseStatus;
import com.mengzhihua.crm.service.dto.CaseStatusRequest;
import com.mengzhihua.crm.service.entity.CrmCase;
import com.mengzhihua.crm.service.repository.CrmCaseRepository;
import com.mengzhihua.crm.service.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CaseServiceTest {
    @Autowired
    private CrmCaseRepository caseRepository;

    @Autowired
    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseRepository.deleteAll();
    }

    @Test
    void calculatesSlaAndCaseNumber() {
        CrmCase crmCase = new CrmCase();
        crmCase.setPriority(CasePriority.HIGH);
        CrmCase saved = caseService.save(crmCase);

        assertEquals(8, CasePriority.HIGH.slaHours());
        assertEquals(14, saved.getCaseNo().length());
        assertEquals(
                CaseStatus.NEW,
                saved.getStatus()
        );
        assertEquals(
                CasePriority.HIGH,
                saved.getPriority()
        );
        assertEquals(
                true,
                saved.getSlaDueAt().isAfter(LocalDateTime.now())
        );
    }

    @Test
    void acceptsValidTransitionAndRejectsInvalidTransition() {
        CrmCase crmCase = caseService.save(new CrmCase());
        CaseStatusRequest inProgress = new CaseStatusRequest();
        inProgress.setStatus(CaseStatus.IN_PROGRESS);
        caseService.changeStatus(crmCase.getId(), inProgress);

        CaseStatusRequest closed = new CaseStatusRequest();
        closed.setStatus(CaseStatus.CLOSED);
        assertThrows(
                BizException.class,
                () -> caseService.changeStatus(crmCase.getId(), closed)
        );
    }

    @Test
    void escalatesPriority() {
        CrmCase crmCase = new CrmCase();
        crmCase.setPriority(CasePriority.MEDIUM);
        crmCase = caseService.save(crmCase);

        CrmCase escalated = caseService.escalate(crmCase.getId());

        assertEquals(CasePriority.HIGH, escalated.getPriority());
        assertEquals(CaseStatus.ESCALATED, escalated.getStatus());
    }
}
