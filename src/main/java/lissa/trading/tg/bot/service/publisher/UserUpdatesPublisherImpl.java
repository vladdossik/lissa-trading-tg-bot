package lissa.trading.tg.bot.service.publisher;

import lissa.trading.tg.bot.dto.notification.NotificationFavouriteStockDto;
import lissa.trading.tg.bot.dto.notification.OperationEnum;
import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import lissa.trading.tg.bot.mapper.FavouriteStockMapper;
import lissa.trading.tg.bot.mapper.UserMapper;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.service.consumer.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUpdatesPublisherImpl implements UserUpdatesPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationContext notificationContext;
    private final UserMapper userMapper;
    private final FavouriteStockMapper favouriteStockMapper;

    @Value("${integration.rabbit.inbound.user-service.exchange}")
    private String exchangeName;

    @Value("${integration.rabbit.outbound.tg-bot.user-update.routing-key}")
    private String tgBotUpdateQueueRoutingKey;

    @Value("${integration.rabbit.outbound.tg-bot.favourite-stocks.routing-key}")
    private String tgBotFavouriteStocksQueueRoutingKey;

    @Override
    public void publishUserUpdateNotification(UserEntity user, OperationEnum operationEnum) {
        if (notificationContext.isExternalSource()) {
            log.info("external-source user update, returning");
            return;
        }
        UserUpdateNotificationDto updateDto = userMapper.toUserUpdateNotificationDto(user);
        updateDto.setOperation(operationEnum);
        rabbitTemplate.convertAndSend(exchangeName, tgBotUpdateQueueRoutingKey, updateDto);
        log.info("published user update notification for: {}", user.getExternalId());
    }

    @Override
    public void publishUserFavoriteStocksUpdateNotification(UserEntity user) {
        if (notificationContext.isExternalSource()) {
            log.info("external-source stocks update, returning");
            return;
        }
        List<NotificationFavouriteStockDto> favoriteStocksNotificationDtoList = favouriteStockMapper
                .toNotificationFavouriteStockDtoList(new ArrayList<>(user.getFavouriteStocks()));
        rabbitTemplate.convertAndSend(exchangeName, tgBotFavouriteStocksQueueRoutingKey,
                                      UserFavoriteStocksUpdateDto.builder()
                                              .favoriteStocks(favoriteStocksNotificationDtoList)
                                              .externalId(user.getExternalId())
                                              .build());
        log.info("published user favorite stocks update notification for: {}", user.getExternalId());
    }
}
