package lissa.trading.tg.bot.tinkoff.dto.stock;

import lissa.trading.tg.bot.tinkoff.dto.Stock;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StocksDto {
    private List<Stock> stocks;
}
