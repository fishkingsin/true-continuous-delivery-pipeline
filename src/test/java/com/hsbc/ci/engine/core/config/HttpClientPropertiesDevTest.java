package com.hsbc.ci.engine.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("dev")
@SpringBootTest(classes = com.hsbc.ci.engine.core.CiEngineApplication.class)
@TestPropertySource(properties = {
    "app.http.timeout.connect=PT1S",
    "app.http.timeout.read=PT5S",
    "app.http.proxy.host=DEV_PROXY_HOST",
    "app.http.proxy.port=8080",
    "app.http.proxy.non-proxy-hosts=localhost|127.0.0.1",
    "app.http.retry.attempts=3",
    "app.http.retry.backoff=PT0.2S"
})
class HttpClientPropertiesDevTest {

    @Autowired
    private HttpClientProperties props;

    @Test
    void testDevProfileProperties() {
        assertNotNull(props);
        assertNotNull(props.getTimeout());
        assertEquals(Duration.ofSeconds(1), props.getTimeout().getConnect());
        assertEquals(Duration.ofSeconds(5), props.getTimeout().getRead());

        assertNotNull(props.getProxy());
        assertEquals("DEV_PROXY_HOST", props.getProxy().getHost());
        assertEquals(Integer.valueOf(8080), props.getProxy().getPort());

        assertNotNull(props.getRetry());
        assertEquals(Integer.valueOf(3), props.getRetry().getAttempts());
        assertEquals(Duration.ofMillis(200), props.getRetry().getBackoff());
    }
}
