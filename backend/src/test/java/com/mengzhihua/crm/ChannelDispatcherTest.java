package com.mengzhihua.crm;

import com.mengzhihua.crm.channel.entity.ChannelConfig;
import com.mengzhihua.crm.channel.entity.ChannelDelivery;
import com.mengzhihua.crm.channel.repository.ChannelConfigRepository;
import com.mengzhihua.crm.channel.service.ChannelDispatcher;
import com.mengzhihua.crm.common.enums.ChannelType;
import com.mengzhihua.crm.common.enums.DeliveryStatus;
import com.mengzhihua.crm.common.enums.NotificationType;
import com.mengzhihua.crm.notification.entity.Notification;
import com.mengzhihua.crm.notification.repository.NotificationRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ChannelDispatcherTest {
    @Autowired
    private ChannelDispatcher dispatcher;

    @Autowired
    private ChannelConfigRepository configRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        configRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @Test
    void disabledEmailCreatesSkippedDelivery() {
        ChannelConfig config = new ChannelConfig();
        config.setName("测试邮件");
        config.setType(ChannelType.EMAIL);
        config.setEnabled(true);
        config.setEventTypes("SYSTEM");
        configRepository.save(config);
        Notification notification = new Notification();
        notification.setRecipient("admin");
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle("测试");
        notification.setContent("测试");
        notification = notificationRepository.save(notification);
        ChannelDelivery delivery = dispatcher.dispatchSync(notification);
        assertEquals(DeliveryStatus.SKIPPED, delivery.getStatus());
    }

    @Test
    void webhookPostsJsonWithSignature() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> signature = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/hook", exchange -> capture(exchange, body, signature));
        server.start();
        try {
            ChannelConfig config = new ChannelConfig();
            config.setName("测试 Webhook");
            config.setType(ChannelType.WEBHOOK);
            config.setEnabled(true);
            config.setTarget("http://127.0.0.1:" + server.getAddress().getPort() + "/hook");
            config.setSecret("secret");
            config.setEventTypes("SYSTEM");
            configRepository.save(config);
            Notification notification = new Notification();
            notification.setRecipient("admin");
            notification.setType(NotificationType.SYSTEM);
            notification.setTitle("Webhook 测试");
            notification.setContent("内容");
            notification = notificationRepository.save(notification);
            ChannelDelivery delivery = dispatcher.dispatchSync(notification);
            assertEquals(DeliveryStatus.SUCCESS, delivery.getStatus());
            assertTrue(body.get().contains("Webhook 测试"));
            assertTrue(signature.get() != null && !signature.get().isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void webhookAcceptsUppercaseHost() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> signature = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/hook", exchange -> capture(exchange, body, signature));
        server.start();
        try {
            ChannelConfig config = new ChannelConfig();
            config.setName("大写主机 Webhook");
            config.setType(ChannelType.WEBHOOK);
            config.setEnabled(true);
            config.setTarget(
                    "http://LOCALHOST:" + server.getAddress().getPort() + "/hook"
            );
            config.setEventTypes("SYSTEM");
            configRepository.save(config);
            Notification notification = new Notification();
            notification.setRecipient("admin");
            notification.setType(NotificationType.SYSTEM);
            notification.setTitle("大写主机测试");
            notification.setContent("内容");
            notification = notificationRepository.save(notification);

            ChannelDelivery delivery = dispatcher.dispatchSync(notification);

            assertEquals(DeliveryStatus.SUCCESS, delivery.getStatus());
            assertTrue(body.get().contains("大写主机测试"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void deliverToIgnoresChannelEnabledAndEventTypes() {
        ChannelConfig config = new ChannelConfig();
        config.setName("定向测试");
        config.setType(ChannelType.EMAIL);
        config.setEnabled(false);
        config.setEventTypes("APPROVAL");
        config = configRepository.save(config);
        Notification notification = new Notification();
        notification.setRecipient("admin");
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle("定向测试");
        notification.setContent("内容");
        notification = notificationRepository.save(notification);

        ChannelDelivery delivery = dispatcher.deliverTo(config, notification);

        assertEquals(DeliveryStatus.SKIPPED, delivery.getStatus());
        assertEquals(1, configRepository.count());
    }

    @Test
    void webhookDoesNotFollowRedirects() throws Exception {
        AtomicInteger privateHits = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().set(
                    "Location",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/private"
            );
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/private", exchange -> {
            privateHits.incrementAndGet();
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        server.start();
        try {
            ChannelConfig config = new ChannelConfig();
            config.setName("重定向测试");
            config.setType(ChannelType.WEBHOOK);
            config.setEnabled(true);
            config.setTarget(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/redirect"
            );
            config.setEventTypes("SYSTEM");
            configRepository.save(config);
            Notification notification = new Notification();
            notification.setRecipient("admin");
            notification.setType(NotificationType.SYSTEM);
            notification.setTitle("重定向测试");
            notification.setContent("内容");
            notification = notificationRepository.save(notification);

            ChannelDelivery delivery = dispatcher.dispatchSync(notification);

            assertEquals(DeliveryStatus.FAILED, delivery.getStatus());
            assertEquals(0, privateHits.get());
        } finally {
            server.stop(0);
        }
    }

    private void capture(
            HttpExchange exchange,
            AtomicReference<String> body,
            AtomicReference<String> signature
    ) throws IOException {
        signature.set(exchange.getRequestHeaders().getFirst("X-CRM-Signature"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = exchange.getRequestBody().read(buffer)) >= 0) {
            output.write(buffer, 0, length);
        }
        body.set(new String(output.toByteArray(), StandardCharsets.UTF_8));
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
