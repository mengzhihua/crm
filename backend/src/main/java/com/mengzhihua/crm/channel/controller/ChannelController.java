package com.mengzhihua.crm.channel.controller;

import com.mengzhihua.crm.channel.entity.ChannelConfig;
import com.mengzhihua.crm.channel.entity.ChannelDelivery;
import com.mengzhihua.crm.channel.repository.ChannelConfigRepository;
import com.mengzhihua.crm.channel.repository.ChannelDeliveryRepository;
import com.mengzhihua.crm.channel.service.ChannelDispatcher;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.ChannelType;
import com.mengzhihua.crm.common.enums.NotificationType;
import com.mengzhihua.crm.notification.entity.Notification;
import com.mengzhihua.crm.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
@PreAuthorize("hasRole('ADMIN')")
public class ChannelController {
    private final ChannelConfigRepository configRepository;
    private final ChannelDeliveryRepository deliveryRepository;
    private final ChannelDispatcher dispatcher;
    private final NotificationRepository notificationRepository;

    public ChannelController(
            ChannelConfigRepository configRepository,
            ChannelDeliveryRepository deliveryRepository,
            ChannelDispatcher dispatcher,
            NotificationRepository notificationRepository
    ) {
        this.configRepository = configRepository;
        this.deliveryRepository = deliveryRepository;
        this.dispatcher = dispatcher;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public Result<java.util.List<ChannelConfig>> list() {
        return Result.ok(configRepository.findAll());
    }

    @PostMapping
    public Result<ChannelConfig> add(@RequestBody ChannelConfig config) {
        validate(config);
        return Result.ok(configRepository.save(config));
    }

    @PutMapping("/{id}")
    public Result<ChannelConfig> edit(
            @PathVariable Long id,
            @RequestBody ChannelConfig config
    ) {
        ChannelConfig current = configRepository.findById(id)
                .orElseThrow(() -> new com.mengzhihua.crm.common.BizException("渠道不存在"));
        if (config.getSecret() == null || config.getSecret().trim().isEmpty()) {
            config.setSecret(current.getSecret());
        }
        validate(config);
        config.setId(id);
        return Result.ok(configRepository.save(config));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        configRepository.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    public Result<ChannelDelivery> test(@PathVariable Long id) {
        ChannelConfig config = configRepository.findById(id)
                .orElseThrow(() -> new com.mengzhihua.crm.common.BizException("渠道不存在"));
        Notification notification = new Notification();
        notification.setRecipient("admin");
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle("CRM 渠道测试");
        notification.setContent("这是一条渠道测试消息");
        notification = notificationRepository.save(notification);
        return Result.ok(dispatcher.deliverTo(config, notification));
    }

    @GetMapping("/deliveries")
    public Result<PageResult<ChannelDelivery>> deliveries(
            @RequestParam Long channelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ChannelDelivery> result = deliveryRepository.findByChannelId(
                channelId,
                DtoUtil.pageable(page, size)
        );
        return Result.ok(DtoUtil.page(result, item -> (ChannelDelivery) item));
    }

    private void validate(ChannelConfig config) {
        if (config.getType() == ChannelType.WEBHOOK) {
            dispatcher.validateWebhookTarget(config.getTarget());
        }
    }
}
