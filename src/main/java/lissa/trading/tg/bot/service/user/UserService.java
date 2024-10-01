package lissa.trading.tg.bot.service.user;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registerUser(SignupRequest signupRequest);

    UserInfoDto getUserInfoFromContext();

    UserInfoDto getUserByTelegramNickname(String telegramNickname);

    boolean userExistsByTelegramNickname(String telegramNickname);

    boolean userExistsByFirstName(String firstName);

    void updateUserToken(String telegramNickname, String newToken);
}
