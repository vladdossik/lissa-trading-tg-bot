package lissa.trading.tg.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "integration.rest")
@Data
public class IntegrationServicesProperties {
    private Map<String, ServiceConfig> services = new HashMap<>();

    @Data
    public static class ServiceConfig {
        private String url;
        private String token;
    }
}