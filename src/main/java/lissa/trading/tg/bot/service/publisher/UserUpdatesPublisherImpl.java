package lissa.trading.tg.bot.service.publisher;

import lissa.trading.tg.bot.dto.notification.OperationEnum;
import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import lissa.trading.tg.bot.mapper.UserMapper;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.service.consumer.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUpdatesPublisherImpl implements UserUpdatesPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationContext notificationContext;
    private final UserMapper userMapper;

    @Value("${integration.rabbit.user-service.exchange.name}")
    private String exchangeName;

    @Value("${integration.rabbit.user-service.user-update-queue.routing-key}")
    private String userUpdateRoutingKey;

    @Value("${integration.rabbit.user-service.favourite-stocks-queue.routing-key}")
    private String favouriteStocksRoutingKey;

    @Override
    public void publishUserUpdateNotification(UserEntity user, OperationEnum operationEnum) {
        if (notificationContext.isExternalSource()) {
            log.info("external-source user update, returning");
            return;
        }
        UserUpdateNotificationDto updateDto = userMapper.toUserUpdateNotificationDto(user);
        updateDto.setOperation(operationEnum);
        rabbitTemplate.convertAndSend(exchangeName, userUpdateRoutingKey, updateDto);
        log.info("published user update notification: {}", updateDto);
    }

    @Override
    public void publishUserFavoriteStocksUpdateNotification(UserEntity user) {
        if (notificationContext.isExternalSource()) {
            return;
        }
        rabbitTemplate.convertAndSend(exchangeName, favouriteStocksRoutingKey,
                                      UserFavoriteStocksUpdateDto.builder()
                                              .favoriteStocksEntity(new ArrayList<>(user.getFavouriteStocks()))
                                              .externalId(user.getExternalId())
                                              .build());
        log.info("published user favorite stocks update notification for: {}", user);
    }

}
