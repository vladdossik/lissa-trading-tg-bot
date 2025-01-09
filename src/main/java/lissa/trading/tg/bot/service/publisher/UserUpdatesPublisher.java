package lissa.trading.tg.bot.service.publisher;

import lissa.trading.tg.bot.dto.notification.OperationEnum;
import lissa.trading.tg.bot.model.UserEntity;

public interface UserUpdatesPublisher {
    void publishUserUpdateNotification(UserEntity user, OperationEnum operationEnum);

    void publishUserFavoriteStocksUpdateNotification(UserEntity user);
}
