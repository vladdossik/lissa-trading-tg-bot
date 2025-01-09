package lissa.trading.tg.bot.service.consumer;

import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import lissa.trading.tg.bot.mapper.UserMapper;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUpdatesNotificationConsumerImpl implements UserUpdatesNotificationConsumer {

    private final NotificationContext notificationContext;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Value("${integration.rabbit.user-service.user-update-queue.routing-key}")
    private String userUpdateRoutingKey;

    @RabbitListener(queues = "${integration.rabbit.user-service.user-update-queue.name}")
    @Transactional
    @Override
    public void receiveUserUpdateNotification(UserUpdateNotificationDto userUpdateDto,
                                              @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("received user update notification: {}", userUpdateDto);
        if(routingKey.equals(userUpdateRoutingKey)) {
            return;
        }
        notificationContext.setFromExternalSource(true);
        log.info("setted context {}", notificationContext);
        try {
            log.info("processing user update notification: {}", userUpdateDto);
            processUserUpdateNotification(userUpdateDto);
        }
        finally {
            notificationContext.clear();
        }
    }

    @RabbitListener(queues = "${integration.rabbit.user-service.favourite-stocks-queue.name}")
    @Transactional
    @Override
    public void receiveUserFavoriteStocksUpdateNotification(UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto) {
        log.info("received user update notification: {}", userFavoriteStocksUpdateDto);
        notificationContext.setFromExternalSource(true);
        log.info("setted context {}", notificationContext);
        processUserFavoriteStocksUpdateNotification(userFavoriteStocksUpdateDto);
    }

    private void processUserUpdateNotification(UserUpdateNotificationDto userUpdate) {
        switch (userUpdate.getOperation()) {
            case REGISTER:
                registerUser(userUpdate);
                return;
            case UPDATE:
                updateUser(userUpdate);
                return;
            case DELETE:
                deleteUser(userUpdate);
        }
    }

    private void processUserFavoriteStocksUpdateNotification(
            UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto) {
        log.info("updating favorite stocks: {}", userFavoriteStocksUpdateDto);
        userService.updateUserFavouriteStocks(userFavoriteStocksUpdateDto.getExternalId(),
                                              userFavoriteStocksUpdateDto.getFavoriteStocksEntity());
    }

    private void registerUser(UserUpdateNotificationDto userUpdate) {
        if (userRepository.existsByTelegramNickname(userUpdate.getTelegramNickname())) {
            updateUser(userUpdate);
        }
        log.info("registering user: {}", userUpdate);
        userService.registerUser(userMapper.toSignupRequest(userUpdate));
    }

    private void updateUser(UserUpdateNotificationDto userUpdate) {
        if (!userRepository.existsByTelegramNickname(userUpdate.getTelegramNickname())) {
            registerUser(userUpdate);
        }
        log.info("updating user: {}", userUpdate);
        userService.updateUserInformation(userUpdate.getExternalId(),
                                          userMapper.toUserPatchDto(userUpdate));
    }

    private void deleteUser(UserUpdateNotificationDto userUpdate) {
        log.info("deleting user: {}", userUpdate);
        userService.deleteUser(userUpdate.getExternalId());
    }
}
