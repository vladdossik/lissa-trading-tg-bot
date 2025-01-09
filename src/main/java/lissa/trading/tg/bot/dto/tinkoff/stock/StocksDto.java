package lissa.trading.tg.bot.dto.tinkoff.stock;

import lissa.trading.tg.bot.dto.tinkoff.Stock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StocksDto {
    private List<Stock> stocks;
}
