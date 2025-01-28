package lissa.trading.tg.bot.config;

import feign.RequestInterceptor;
import lissa.trading.tg.bot.feign.InternalTokenFeignInterceptor;
import lissa.trading.tg.bot.security.internal.InternalTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class InternalFeignConfig {

    private final InternalTokenService tokenService;

    @Bean
    public RequestInterceptor internalTokenInterceptor() {
        return new InternalTokenFeignInterceptor(tokenService);
    }
}