package com.mengzhihua.crm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengzhihua.crm.audit.entity.AuditLog;
import com.mengzhihua.crm.audit.repository.AuditLogRepository;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.common.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setDisplayName("管理员");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    @Test
    void recordsAuthenticatedMutationAndLogin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        String token = body.get("data").get("token").asText();

        mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"审计线索\",\"company\":\"审计公司\"}"))
                .andExpect(status().isOk());

        AuditLog mutation = auditLogRepository.findAll().stream()
                .filter(item -> "POST /api/leads".equals(item.getAction()))
                .findFirst()
                .get();
        assertEquals("admin", mutation.getUsername());
        assertEquals(Role.ADMIN, mutation.getRole());
        assertEquals("leads", mutation.getModule());

        assertEquals(1, auditLogRepository.findAll().stream()
                .filter(item -> "LOGIN".equals(item.getAction()))
                .count());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        assertEquals(1, auditLogRepository.findAll().stream()
                .filter(item -> "LOGIN_FAILED".equals(item.getAction()))
                .count());
    }
}
