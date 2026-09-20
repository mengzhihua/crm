package com.mengzhihua.crm;

import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.NotificationType;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.notification.entity.Notification;
import com.mengzhihua.crm.notification.repository.NotificationRepository;
import com.mengzhihua.crm.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        User manager = new User();
        manager.setUsername("manager");
        manager.setRole(Role.SALES_MANAGER);
        manager.setEnabled(true);
        manager.setPassword("test");
        userRepository.save(manager);
    }

    @Test
    @WithMockUser(username = "manager", roles = "SALES_MANAGER")
    void sendsToRoleAndCountsUnread() {
        notificationService.sendToRole(
                Role.SALES_MANAGER,
                NotificationType.APPROVAL,
                "待审批：报价",
                "请审批",
                RelatedType.QUOTE,
                1L
        );
        assertEquals(1, notificationService.unreadCount());
    }

    @Test
    @WithMockUser(username = "manager", roles = "SALES_MANAGER")
    void cannotMarkAnotherUsersNotification() {
        Notification notification = notificationService.send(
                "other",
                NotificationType.SYSTEM,
                "系统",
                "内容",
                null,
                null
        );
        assertThrows(
                BizException.class,
                () -> notificationService.markRead(notification.getId())
        );
    }
}
