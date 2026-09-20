package com.mengzhihua.crm.notification.repository;

import com.mengzhihua.crm.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {
    long countByRecipientAndReadFalse(String recipient);
}
