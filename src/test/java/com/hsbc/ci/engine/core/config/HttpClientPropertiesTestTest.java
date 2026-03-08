package com.hsbc.ci.engine.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(classes = com.hsbc.ci.engine.core.CiEngineApplication.class)
@TestPropertySource(properties = {
    "app.http.timeout.connect=PT2S",
    "app.http.timeout.read=PT10S",
    "app.http.proxy.host=TEST_PROXY_HOST",
    "app.http.proxy.port=8081",
    "app.http.proxy.non-proxy-hosts=localhost",
    "app.http.retry.attempts=1",
    "app.http.retry.backoff=PT0.1S"
})
class HttpClientPropertiesTestTest {

    @Autowired
    private HttpClientProperties props;

    @Test
    void testProfileProperties() {
        assertNotNull(props);
        assertNotNull(props.getTimeout());
        assertEquals(Duration.ofSeconds(2), props.getTimeout().getConnect());
        assertEquals(Duration.ofSeconds(10), props.getTimeout().getRead());

        assertNotNull(props.getProxy());
        assertEquals("TEST_PROXY_HOST", props.getProxy().getHost());
        assertEquals(Integer.valueOf(8081), props.getProxy().getPort());
        assertEquals(List.of("localhost"), props.getProxy().getNonProxyHosts());

        assertNotNull(props.getRetry());
        assertEquals(Integer.valueOf(1), props.getRetry().getAttempts());
        assertEquals(Duration.ofMillis(100), props.getRetry().getBackoff());
    }
}
