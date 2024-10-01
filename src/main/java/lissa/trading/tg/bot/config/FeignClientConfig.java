package lissa.trading.tg.bot.config;

import feign.RequestInterceptor;
import lissa.trading.tg.bot.bot.ChatIdContext;
import lissa.trading.tg.bot.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignClientConfig {

    private final UserTokenService userTokenService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Long chatId = ChatIdContext.getChatId();  // Извлекаем chatId из контекста
            if (chatId != null && userTokenService.hasJwtToken(chatId)) {
                String jwtToken = userTokenService.getJwtToken(chatId);
                requestTemplate.header("Authorization", "Bearer " + jwtToken);
            } else {
                log.warn("JWT token or chatId not found, skipping authorization header.");
            }
        };
    }
}