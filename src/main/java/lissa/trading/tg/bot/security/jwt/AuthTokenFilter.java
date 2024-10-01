package lissa.trading.tg.bot.security.jwt;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.feign.AuthServiceClient;
import lissa.trading.lissa.auth.lib.security.BaseAuthTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends BaseAuthTokenFilter<UserInfoDto> {

    private final AuthServiceClient authServiceClient;

    @Override
    protected List<String> parseRoles(UserInfoDto userInfo) {
        return userInfo.getRoles();
    }

    @Override
    protected UserInfoDto retrieveUserInfo(String token) {
        return authServiceClient.getUserInfo("Bearer " + token);
    }
}