package com.marketplace.infrastructure.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private Rule login = new Rule(5, 60);
    private Rule register = new Rule(3, 60);
    private Rule deliveryGenerate = new Rule(3, 600);
    private Rule deliveryVerify = new Rule(5, 600);
    private Rule general = new Rule(100, 60);

    @Data
    public static class Rule {
        private int maxRequests;
        private int windowSeconds;

        public Rule() {}

        public Rule(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
}
