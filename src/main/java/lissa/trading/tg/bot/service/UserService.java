package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;

import java.util.Optional;

public interface UserService {
    UserRegistrationResponse registerUser(SignupRequest signupRequest);

    Optional<UserInfoDto> getUserByTelegramNickname(String telegramNickname);

    void updateUserToken(String telegramNickname, String newToken);
}
