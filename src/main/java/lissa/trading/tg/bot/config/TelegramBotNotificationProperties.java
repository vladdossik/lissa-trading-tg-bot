package lissa.trading.tg.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "telegram-bot.notification")
@Data
public class TelegramBotNotificationProperties {
    private long checkPriceChangesInterval;
    private long priceChangeWindowMinutes;
    private double criticalPriceChangePercentage;
}