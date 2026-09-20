package com.mengzhihua.crm.approval.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApprovalDataMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(
            ApprovalDataMigrator.class
    );

    private final JdbcTemplate jdbcTemplate;

    public ApprovalDataMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateRuleRoles();
        migrateRequests();
        backfillVersion("crm_approval_request");
        backfillVersion("crm_approval_step");
    }

    private void backfillVersion(String table) {
        try {
            jdbcTemplate.update(
                    "UPDATE " + table + " SET version = 0 WHERE version IS NULL"
            );
        } catch (RuntimeException exception) {
            log.warn("{} 版本号回填失败，继续启动", table, exception);
        }
    }

    private void migrateRuleRoles() {
        try {
            jdbcTemplate.update(
                    "UPDATE crm_approval_rule SET approver_roles = approver_role "
                            + "WHERE approver_roles IS NULL "
                            + "AND approver_role IS NOT NULL"
            );
        } catch (RuntimeException exception) {
            log.warn("审批规则角色迁移失败，继续启动", exception);
        }
    }

    private void migrateRequests() {
        List<Map<String, Object>> requests;
        try {
            requests = jdbcTemplate.queryForList(
                    "SELECT id, approver_role, status, owner "
                            + "FROM crm_approval_request "
                            + "WHERE total_steps IS NULL OR total_steps = 0"
            );
        } catch (RuntimeException exception) {
            log.warn("审批申请迁移查询失败，继续启动", exception);
            return;
        }
        for (Map<String, Object> request : requests) {
            Number id = (Number) request.get("id");
            String role = (String) request.get("approver_role");
            String status = (String) request.get("status");
            if (status == null) {
                status = "PENDING";
            }
            updateRequest(id);
            insertStepIfMissing(id, role, status, request.get("owner"));
        }
    }

    private void updateRequest(Number id) {
        try {
            jdbcTemplate.update(
                    "UPDATE crm_approval_request SET current_step = 1, "
                            + "total_steps = 1 WHERE id = ?",
                    id.longValue()
            );
        } catch (RuntimeException exception) {
            log.warn("审批申请 {} 字段迁移失败，继续处理", id, exception);
        }
    }

    private void insertStepIfMissing(
            Number requestId,
            String role,
            String status,
            Object owner
    ) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM crm_approval_step WHERE request_id = ?",
                    Integer.class,
                    requestId.longValue()
            );
            if (count != null && count > 0) {
                return;
            }
        } catch (RuntimeException exception) {
            log.warn("审批步骤 {} 检查失败，继续处理", requestId, exception);
            return;
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO crm_approval_step "
                            + "(request_id, step_order, approver_role, status, "
                            + "created_at, updated_at, owner, version) "
                            + "VALUES (?, 1, ?, ?, CURRENT_TIMESTAMP, "
                            + "CURRENT_TIMESTAMP, ?, 0)",
                    requestId.longValue(),
                    role,
                    status,
                    owner
            );
        } catch (RuntimeException exception) {
            log.warn("审批步骤 {} 迁移失败，继续启动", requestId, exception);
        }
    }
}
