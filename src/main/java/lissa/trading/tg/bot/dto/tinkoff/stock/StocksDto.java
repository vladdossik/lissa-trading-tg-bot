package lissa.trading.tg.bot.dto.tinkoff.stock;

import lissa.trading.tg.bot.dto.tinkoff.Stock;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StocksDto {
    private List<Stock> stocks;
}
