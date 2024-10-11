package lissa.trading.tg.bot.notification;

import lissa.trading.tg.bot.bot.TelegramBot;
import lissa.trading.tg.bot.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TelegramBot telegramBot;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveNotification(NotificationMessage message) {
        log.debug("Получено уведомление: {}", message);
        try {
            telegramBot.sendNotification(message);
            log.info("Уведомление отправлено пользователю {} для акции {}",
                    message.getChatId(), message.getStockName());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления пользователю {}: {}", message.getChatId(), e.getMessage(), e);
        }
    }
}
