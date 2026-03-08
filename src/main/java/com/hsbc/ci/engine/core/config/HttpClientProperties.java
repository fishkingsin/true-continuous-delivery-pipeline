package com.hsbc.ci.engine.core.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.http")
public class HttpClientProperties {

    private Timeout timeout = new Timeout();
    private Proxy proxy = new Proxy();
    private Retry retry = new Retry();

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public static class Timeout {

        private Duration connect;
        private Duration read;

        public Duration getConnect() {
            return connect;
        }

        public void setConnect(Duration connect) {
            this.connect = connect;
        }

        public Duration getRead() {
            return read;
        }

        public void setRead(Duration read) {
            this.read = read;
        }
    }

    public static class Proxy {

        private String host;
        private Integer port;
        private List<String> nonProxyHosts = new ArrayList<>();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public List<String> getNonProxyHosts() {
            return nonProxyHosts;
        }

        public void setNonProxyHosts(List<String> nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
        }
    }

    public static class Retry {

        private Integer attempts;
        private Duration backoff;

        public Integer getAttempts() {
            return attempts;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }

        public Duration getBackoff() {
            return backoff;
        }

        public void setBackoff(Duration backoff) {
            this.backoff = backoff;
        }
    }
}
