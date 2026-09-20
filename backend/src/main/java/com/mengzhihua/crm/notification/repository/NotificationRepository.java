package com.mengzhihua.crm.notification.repository;

import com.mengzhihua.crm.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {
    long countByRecipientAndReadFalse(String recipient);

    @Modifying
    @Transactional
    @Query("update Notification n set n.read = true, n.readAt = :now "
            + "where n.recipient = :recipient and n.read = false")
    int markAllRead(
            @Param("recipient") String recipient,
            @Param("now") java.time.LocalDateTime now
    );
}
