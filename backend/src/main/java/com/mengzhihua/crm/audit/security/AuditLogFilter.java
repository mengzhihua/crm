package com.mengzhihua.crm.audit.security;

import com.mengzhihua.crm.audit.service.AuditLogService;
import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.enums.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuditLogFilter extends OncePerRequestFilter {
    private final AuditLogService auditLogService;

    public AuditLogFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean shouldRecord = path.startsWith("/api/")
                && ("POST".equals(method)
                || "PUT".equals(method)
                || "DELETE".equals(method))
                && !"/api/auth/login".equals(path);
        long started = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (shouldRecord) {
                try {
                    String username = CurrentUser.username();
                    Role role = CurrentUser.role();
                    String action = method + " " + path;
                    auditLogService.record(
                            action,
                            username,
                            role,
                            method,
                            path,
                            request.getQueryString(),
                            response.getStatus(),
                            System.currentTimeMillis() - started,
                            request.getRemoteAddr()
                    );
                } catch (RuntimeException ignored) {
                    // Audit must never change the business response.
                }
            }
        }
    }
}
