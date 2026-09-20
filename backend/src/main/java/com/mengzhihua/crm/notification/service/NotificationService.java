package com.mengzhihua.crm.notification.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.NotificationType;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.notification.entity.Notification;
import com.mengzhihua.crm.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification send(
            String recipient,
            NotificationType type,
            String title,
            String content,
            RelatedType relatedType,
            Long relatedId
    ) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void sendToRole(
            Role role,
            NotificationType type,
            String title,
            String content,
            RelatedType relatedType,
            Long relatedId
    ) {
        userRepository.findAll().stream()
                .filter(user -> user.isEnabled() && user.getRole() == role)
                .forEach(user -> send(
                        user.getUsername(),
                        type,
                        title,
                        content,
                        relatedType,
                        relatedId
                ));
    }

    public PageResult<Notification> list(
            int page,
            int size,
            Boolean unreadOnly
    ) {
        String recipient = CurrentUser.usernameOrDefault();
        Specification<Notification> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("recipient"), recipient));
            if (Boolean.TRUE.equals(unreadOnly)) {
                predicates.add(builder.isFalse(root.get("read")));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Notification> result = notificationRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Notification) item);
    }

    public long unreadCount() {
        return notificationRepository.countByRecipientAndReadFalse(
                CurrentUser.usernameOrDefault()
        );
    }

    @Transactional
    public Notification markRead(Long id) {
        Notification notification = owned(id);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead() {
        PageResult<Notification> page = list(1, 10000, true);
        for (Notification notification : page.getRecords()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private Notification owned(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BizException("通知不存在"));
        if (!CurrentUser.usernameOrDefault().equals(notification.getRecipient())) {
            throw new BizException("无权操作该通知");
        }
        return notification;
    }
}
