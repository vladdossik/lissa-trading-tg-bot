package lissa.trading.tg.bot.tinkoff.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class InternalTokenFeignInterceptor implements RequestInterceptor {

    @Value("${integration.rest.tinkoff-api-service.token}")
    private String internalToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization",
                new String(Base64.getDecoder().decode(internalToken), StandardCharsets.UTF_8).trim());
    }
}
