package lissa.trading.tg.bot.notification;

import lissa.trading.tg.bot.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final String NOTIFICATION_QUEUE = RabbitMQConfig.NOTIFICATION_QUEUE;

    public void sendNotification(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });

            log.debug("Notification queued for chat ID {}", message.getChatId());
            log.info("Notification successfully sent to user {} for stock {}", message.getChatId(), message.getStockName());
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", message.getChatId(), e.getMessage(), e);
        }
    }
}
