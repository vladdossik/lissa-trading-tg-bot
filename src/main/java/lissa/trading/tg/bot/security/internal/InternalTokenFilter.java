package lissa.trading.tg.bot.security.internal;

import jakarta.servlet.http.HttpServletRequest;
import lissa.trading.lissa.auth.lib.security.BaseAuthTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalTokenFilter extends BaseAuthTokenFilter<String> {

    private final InternalTokenService internalTokenService;

    @Override
    protected boolean validateJwtToken(String token) {
        return internalTokenService.validateInternalToken(token);
    }

    @Override
    protected List<String> parseRoles(String token) {
        return internalTokenService.getRolesFromToken(token);
    }

    @Override
    protected String retrieveUserInfo(String token) {
        return internalTokenService.getServiceNameFromToken(token);
    }

    @Override
    protected boolean shouldSkipFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return !requestURI.startsWith("/v1/internal/"); // Фильтр срабатывает только на внутренних запросах
    }
}
