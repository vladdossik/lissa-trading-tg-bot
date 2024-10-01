package lissa.trading.tg.bot.dto.tinkoff;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Stock {
    private String ticker;
    private String figi;
    private String name;
    private String type;
    private Currency currency;
    private String source;
}
