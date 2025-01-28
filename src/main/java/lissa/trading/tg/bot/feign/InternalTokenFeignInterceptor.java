package lissa.trading.tg.bot.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lissa.trading.tg.bot.security.internal.InternalTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
@Slf4j
public class InternalTokenFeignInterceptor implements RequestInterceptor {

    private final InternalTokenService tokenService;

    @Override
    public void apply(RequestTemplate template) {
        try {
            String baseUrl = getBaseUrl(new URI(template.feignTarget().url()));
            template.header("Authorization", new String(
                    Base64.getDecoder().decode(tokenService.getTokenByUrl(baseUrl)), StandardCharsets.UTF_8).trim());
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBaseUrl(URI uri) throws URISyntaxException {
        return new URI(uri.getScheme(),
                       uri.getAuthority(),
                       null,
                       null,
                       null)
                .toString();
    }
}
