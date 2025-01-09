package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.tg.bot.dto.user.UserPatchDto;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserRegistrationResponse registerUser(SignupRequest signupRequest);

    Optional<UserInfoDto> getUserByTelegramNickname(String telegramNickname);

    void updateUserToken(String telegramNickname, String newToken);

    void updateUserChatId(String telegramNickname, Long chatId);

    void deleteUser(UUID externalId);

    void updateUserInformation(UUID externalId, UserPatchDto userPatchDto);

    void updateUserFavouriteStocks(UUID externalId, List<FavouriteStock> favouriteStocks);

    void deleteUserFavouriteStocks(String telegramNickname, List<String> tickers);

    void addUserFavouriteStocks(String telegramNickname, List<String> tickers);

    Optional<UserEntity> getUserByChatId(Long chatId);

    List<UserEntity> getAllUsers();

}
