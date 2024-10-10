package lissa.trading.tg.bot.service;

import lissa.trading.tg.bot.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceMonitorService {

    private final UserService userService;
    private final UserProcessingService userProcessingService;

    @Scheduled(fixedRateString = "${telegram-bot.notification.checkPriceChangesInterval}")
    public void checkPriceChanges() {
        log.debug("Начало процесса проверки цен");

        List<UserEntity> users = userService.getAllUsers();
        log.debug("Получено {} пользователей", users.size());

        users.stream()
                .filter(user -> user.getTelegramChatId() != null)
                .forEach(user -> userProcessingService.processUserAsync(user.getId())
                        .exceptionally(ex -> {
                            log.error("Error processing user {}: {}", user.getTelegramNickname(), ex.getMessage(), ex);
                            return null;
                        }));

        log.debug("Процесс проверки цен завершен");
    }
}
