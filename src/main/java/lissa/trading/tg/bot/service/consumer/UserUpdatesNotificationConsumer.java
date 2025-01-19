package lissa.trading.tg.bot.service.consumer;

import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import org.springframework.messaging.handler.annotation.Header;

public interface UserUpdatesNotificationConsumer {
    void receiveUserUpdateNotification(UserUpdateNotificationDto userUpdate,
                                       @Header("amqp_receivedRoutingKey") String routingKey);

    void receiveUserFavoriteStocksUpdateNotification(UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto,
                                                     @Header("amqp_receivedRoutingKey") String routingKey);
}
