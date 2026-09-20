package com.mengzhihua.crm;

import com.mengzhihua.crm.approval.config.ApprovalDataMigrator;
import com.mengzhihua.crm.approval.entity.ApprovalRequest;
import com.mengzhihua.crm.approval.entity.ApprovalStep;
import com.mengzhihua.crm.approval.repository.ApprovalRequestRepository;
import com.mengzhihua.crm.approval.repository.ApprovalRuleRepository;
import com.mengzhihua.crm.approval.repository.ApprovalStepRepository;
import com.mengzhihua.crm.common.enums.ApprovalStatus;
import com.mengzhihua.crm.common.enums.ApprovalTargetType;
import com.mengzhihua.crm.common.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ApprovalDataMigratorTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApprovalDataMigrator migrator;

    @Autowired
    private ApprovalRuleRepository ruleRepository;

    @Autowired
    private ApprovalRequestRepository requestRepository;

    @Autowired
    private ApprovalStepRepository stepRepository;

    @BeforeEach
    void setUp() {
        stepRepository.deleteAll();
        requestRepository.deleteAll();
        ruleRepository.deleteAll();
        jdbcTemplate.execute(
                "ALTER TABLE crm_approval_rule "
                        + "ADD COLUMN IF NOT EXISTS approver_role VARCHAR(50)"
        );
    }

    @Test
    void migratesLegacyRuleAndSingleStepRequest() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO crm_approval_rule "
                        + "(name, target_type, min_amount, approver_role, priority, "
                        + "active, created_at, updated_at, owner) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "旧审批规则",
                "QUOTE",
                new BigDecimal("100"),
                "SALES_MANAGER",
                1,
                true,
                now,
                now,
                "admin"
        );
        jdbcTemplate.update(
                "INSERT INTO crm_approval_request "
                        + "(target_type, target_id, title, submitter, approver_role, "
                        + "current_step, total_steps, status, submitted_at, "
                        + "created_at, updated_at, owner, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "QUOTE",
                99L,
                "旧审批申请",
                "sales",
                "SALES_MANAGER",
                0,
                0,
                "PENDING",
                now,
                now,
                now,
                "sales",
                0
        );

        migrator.run(new DefaultApplicationArguments());

        assertEquals(
                "SALES_MANAGER",
                jdbcTemplate.queryForObject(
                        "SELECT approver_roles FROM crm_approval_rule "
                                + "WHERE name = ?",
                        String.class,
                        "旧审批规则"
                )
        );
        ApprovalRequest request = requestRepository.findAll().get(0);
        assertEquals(1, request.getCurrentStep());
        assertEquals(1, request.getTotalSteps());
        ApprovalStep step = stepRepository
                .findByRequestIdOrderByStepOrderAsc(request.getId())
                .get(0);
        assertEquals(1, step.getStepOrder());
        assertEquals(Role.SALES_MANAGER, step.getApproverRole());
        assertEquals(ApprovalStatus.PENDING, step.getStatus());
        assertEquals("sales", step.getOwner());
        assertNotNull(step.getCreatedAt());
        assertNotNull(step.getUpdatedAt());
    }
}
