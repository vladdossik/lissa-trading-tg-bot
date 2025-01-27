package lissa.trading.tg.bot.dto.tinkoff.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {
    private String figi;
    private Double price;
}
