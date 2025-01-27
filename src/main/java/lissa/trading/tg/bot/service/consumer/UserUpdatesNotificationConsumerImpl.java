package lissa.trading.tg.bot.service.consumer;

import lissa.trading.tg.bot.dto.notification.UserFavoriteStocksUpdateDto;
import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import lissa.trading.tg.bot.mapper.FavouriteStockMapper;
import lissa.trading.tg.bot.mapper.UserMapper;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUpdatesNotificationConsumerImpl implements UserUpdatesNotificationConsumer {

    private final NotificationContext notificationContext;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FavouriteStockMapper favouriteStockMapper;

    @RabbitListener(queues = "${integration.rabbit.inbound.user-service.user-update.queue}")
    @Override
    public void receiveUserUpdateNotification(UserUpdateNotificationDto userUpdateDto) {
        log.info("received user update notification: {}", userUpdateDto);
        notificationContext.setFromExternalSource(true);
        processUserUpdateNotification(userUpdateDto);
    }

    @RabbitListener(queues = "${integration.rabbit.inbound.user-service.favourite-stocks.queue}")
    @Override
    public void receiveUserFavoriteStocksUpdateNotification(UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto) {
        log.info("received user favorite stocks update notification: {}", userFavoriteStocksUpdateDto);
        notificationContext.setFromExternalSource(true);
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

    private void processUserFavoriteStocksUpdateNotification(
            UserFavoriteStocksUpdateDto userFavoriteStocksUpdateDto) {
        log.info("updating favorite stocks: {}", userFavoriteStocksUpdateDto);
        userService.setUpdatedFavouriteStocksToUser(
                userFavoriteStocksUpdateDto.getExternalId(), favouriteStockMapper
                        .toFavoriteStocksFromNotificationFavoriteStockDto(
                                userFavoriteStocksUpdateDto.getFavoriteStocks()));
    }
}
