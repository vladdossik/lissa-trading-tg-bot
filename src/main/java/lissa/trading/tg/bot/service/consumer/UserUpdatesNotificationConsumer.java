package lissa.trading.tg.bot.service.consumer;

import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;

public interface UserUpdatesNotificationConsumer {
    void receiveUserUpdateNotification(UserUpdateNotificationDto userUpdate);

    void receiveUserFavoriteStocksUpdateNotification(UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto);
}
