package com.mengzhihua.crm.channel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.channel.entity.ChannelConfig;
import com.mengzhihua.crm.channel.entity.ChannelDelivery;
import com.mengzhihua.crm.channel.repository.ChannelConfigRepository;
import com.mengzhihua.crm.channel.repository.ChannelDeliveryRepository;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.ChannelType;
import com.mengzhihua.crm.common.enums.DeliveryStatus;
import com.mengzhihua.crm.notification.entity.Notification;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

@Service
public class ChannelDispatcher {
    private final ChannelConfigRepository configRepository;
    private final ChannelDeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final ObjectMapper objectMapper;
    private final boolean mailEnabled;
    private final boolean allowPrivateWebhook;

    public ChannelDispatcher(
            ChannelConfigRepository configRepository,
            ChannelDeliveryRepository deliveryRepository,
            UserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSender,
            ObjectMapper objectMapper,
            @Value("${crm.mail.enabled:false}") boolean mailEnabled,
            @Value("${crm.webhook.allow-private:false}") boolean allowPrivateWebhook
    ) {
        this.configRepository = configRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.mailEnabled = mailEnabled;
        this.allowPrivateWebhook = allowPrivateWebhook;
    }

    @Async
    public void dispatch(Notification notification) {
        try {
            dispatchSync(notification);
        } catch (RuntimeException exception) {
            // Channel delivery must never affect notification creation.
        }
    }

    public ChannelDelivery dispatchSync(Notification notification) {
        ChannelDelivery last = null;
        for (ChannelConfig config : configRepository.findAll()) {
            if (!config.isEnabled() || !matches(config, notification)) {
                continue;
            }
            last = deliverTo(config, notification);
        }
        return last;
    }

    public ChannelDelivery deliverTo(
            ChannelConfig config,
            Notification notification
    ) {
        ChannelDelivery delivery = new ChannelDelivery();
        delivery.setChannelId(config.getId());
        delivery.setNotificationId(notification.getId());
        delivery.setAttempts(1);
        try {
            if (config.getType() == ChannelType.EMAIL) {
                deliverEmail(config, notification);
                delivery.setStatus(DeliveryStatus.SUCCESS);
                delivery.setResponse("邮件已发送");
            } else {
                deliverWebhook(config, notification);
                delivery.setStatus(DeliveryStatus.SUCCESS);
                delivery.setResponse("Webhook 已发送");
            }
        } catch (SkippedDeliveryException exception) {
            delivery.setStatus(DeliveryStatus.SKIPPED);
            delivery.setResponse(exception.getMessage());
        } catch (RuntimeException exception) {
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setResponse(exception.getMessage());
        }
        return deliveryRepository.save(delivery);
    }

    private void deliverEmail(
            ChannelConfig config,
            Notification notification
    ) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!mailEnabled || sender == null) {
            throw new SkippedDeliveryException("邮件渠道未启用");
        }
        String target = config.getTarget();
        if (target == null || target.trim().isEmpty()) {
            target = userRepository.findByUsername(notification.getRecipient())
                    .map(User::getEmail)
                    .orElse(null);
        }
        if (target == null || target.trim().isEmpty()) {
            throw new SkippedDeliveryException("邮件目标为空");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(target);
        message.setSubject(notification.getTitle());
        message.setText(notification.getContent());
        sender.send(message);
    }

    private void deliverWebhook(
            ChannelConfig config,
            Notification notification
    ) {
        validateWebhookTarget(config.getTarget());
        String body;
        try {
            body = objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getSecret() != null && !config.getSecret().isEmpty()) {
            headers.set(
                    "X-CRM-Signature",
                    sign(config.getSecret(), body)
            );
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        new RestTemplate(factory).postForEntity(
                config.getTarget(),
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    public void validateWebhookTarget(String target) {
        if (allowPrivateWebhook) {
            return;
        }
        if (target == null || target.trim().isEmpty()) {
            throw new BizException("Webhook 地址不允许指向内网");
        }
        try {
            URI uri = URI.create(target);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme)
                    && !"https".equalsIgnoreCase(scheme)) {
                throw new BizException("Webhook 地址不允许指向内网");
            }
            if (uri.getHost() == null || isPrivate(uri.getHost())) {
                throw new BizException("Webhook 地址不允许指向内网");
            }
        } catch (IllegalArgumentException exception) {
            throw new BizException("Webhook 地址不允许指向内网");
        }
    }

    private boolean isPrivate(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()) {
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException exception) {
            return true;
        }
    }

    private String sign(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("无法生成签名", exception);
        }
    }

    private boolean matches(ChannelConfig config, Notification notification) {
        return config.getEventTypes() == null
                || config.getEventTypes().trim().isEmpty()
                || Arrays.asList(config.getEventTypes().split(",")).stream()
                .map(String::trim)
                .anyMatch(value -> value.equals(notification.getType().name()));
    }

    private static class SkippedDeliveryException extends RuntimeException {
        SkippedDeliveryException(String message) {
            super(message);
        }
    }
}
