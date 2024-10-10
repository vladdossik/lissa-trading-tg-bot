package lissa.trading.tg.bot.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private Long chatId;
    private String stockName;
    private Double currentPrice;
    private Double changePercentage;
    private String currency;
}