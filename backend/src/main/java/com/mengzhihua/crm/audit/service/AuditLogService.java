package com.mengzhihua.crm.audit.service;

import com.mengzhihua.crm.audit.entity.AuditLog;
import com.mengzhihua.crm.audit.repository.AuditLogRepository;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.CsvExportService;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditLog record(String action, String username, int status) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setStatus(status);
        log.setMethod("POST");
        log.setPath("/api/auth/login");
        log.setModule("auth");
        log.setRole(null);
        return repository.save(log);
    }

    public AuditLog record(
            String action,
            String username,
            Role role,
            String method,
            String path,
            String query,
            int status,
            long durationMs,
            String ip
    ) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setRole(role);
        log.setMethod(method);
        log.setPath(path);
        log.setQuery(query);
        log.setStatus(status);
        log.setDurationMs(durationMs);
        log.setIp(ip);
        log.setModule(module(path));
        return repository.save(log);
    }

    public PageResult<AuditLog> list(
            int page,
            int size,
            String username,
            String module,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Specification<AuditLog> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("username"), username.trim()));
            }
            if (module != null && !module.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("module"), module.trim()));
            }
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                ));
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<AuditLog> result = repository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (AuditLog) item);
    }

    public ResponseEntity<byte[]> export(
            String username,
            String module,
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<AuditLog> logs = list(1, 10000, username, module, from, to)
                .getRecords();
        List<List<?>> rows = logs.stream()
                .map(log -> Arrays.asList(
                        log.getCreatedAt(),
                        log.getUsername(),
                        log.getRole(),
                        log.getAction(),
                        log.getModule(),
                        log.getPath(),
                        log.getStatus(),
                        log.getDurationMs(),
                        log.getIp()
                ))
                .collect(Collectors.toList());
        return CsvExportService.download(
                "audit-logs.csv",
                Arrays.asList(
                        "时间", "用户", "角色", "操作", "模块",
                        "路径", "状态", "耗时", "IP"
                ),
                rows
        );
    }

    private String module(String path) {
        if (path == null || !path.startsWith("/api/")) {
            return "";
        }
        String value = path.substring(5);
        int index = value.indexOf('/');
        return index < 0 ? value : value.substring(0, index);
    }
}
