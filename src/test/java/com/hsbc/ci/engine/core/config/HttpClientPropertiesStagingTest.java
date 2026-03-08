package com.hsbc.ci.engine.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("staging")
@SpringBootTest(classes = com.hsbc.ci.engine.core.CiEngineApplication.class)
@TestPropertySource(properties = {
    "app.http.timeout.connect=PT5S",
    "app.http.timeout.read=PT15S",
    "app.http.proxy.host=STAGING_PROXY_HOST",
    "app.http.proxy.port=8082",
    "app.http.proxy.non-proxy-hosts=",
    "app.http.retry.attempts=5",
    "app.http.retry.backoff=PT0.5S"
})
class HttpClientPropertiesStagingTest {

    @Autowired
    private HttpClientProperties props;

    @Test
    void testProfileProperties() {
        assertNotNull(props);
        assertNotNull(props.getTimeout());
        assertEquals(Duration.ofSeconds(5), props.getTimeout().getConnect());
        assertEquals(Duration.ofSeconds(15), props.getTimeout().getRead());

        assertNotNull(props.getProxy());
        assertEquals("STAGING_PROXY_HOST", props.getProxy().getHost());
        assertEquals(Integer.valueOf(8082), props.getProxy().getPort());

        assertNotNull(props.getRetry());
        assertEquals(Integer.valueOf(5), props.getRetry().getAttempts());
        assertEquals(Duration.ofMillis(500), props.getRetry().getBackoff());
    }
}
