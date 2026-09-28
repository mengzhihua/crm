package com.mengzhihua.crm;

import com.mengzhihua.crm.channel.service.ChannelDispatcher;
import com.mengzhihua.crm.common.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "crm.webhook.allow-private=false")
@ActiveProfiles("test")
class ChannelSecurityTest {
    @Autowired
    private ChannelDispatcher dispatcher;

    @Test
    void rejectsPrivateWebhookWhenProtectionEnabled() {
        assertThrows(
                BizException.class,
                () -> dispatcher.validateWebhookTarget("http://127.0.0.1/hook")
        );
    }
}
