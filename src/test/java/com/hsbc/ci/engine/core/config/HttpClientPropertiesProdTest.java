package com.hsbc.ci.engine.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("prod")
@SpringBootTest(classes = com.hsbc.ci.engine.core.CiEngineApplication.class)
@TestPropertySource(properties = {
    "app.http.timeout.connect=PT10S",
    "app.http.timeout.read=PT30S",
    "app.http.proxy.host=PROD_PROXY_HOST",
    "app.http.proxy.port=8080",
    "app.http.proxy.non-proxy-hosts=",
    "app.http.retry.attempts=10",
    "app.http.retry.backoff=PT1S"
})
class HttpClientPropertiesProdTest {

    @Autowired
    private HttpClientProperties props;

    @Test
    void testProfileProperties() {
        assertNotNull(props);
        assertNotNull(props.getTimeout());
        assertEquals(Duration.ofSeconds(10), props.getTimeout().getConnect());
        assertEquals(Duration.ofSeconds(30), props.getTimeout().getRead());

        assertNotNull(props.getProxy());
        assertEquals("PROD_PROXY_HOST", props.getProxy().getHost());
        assertEquals(Integer.valueOf(8080), props.getProxy().getPort());

        assertNotNull(props.getRetry());
        assertEquals(Integer.valueOf(10), props.getRetry().getAttempts());
        assertEquals(Duration.ofSeconds(1), props.getRetry().getBackoff());
    }
}
