package lissa.trading.tg.bot.tinkoff.dto.stock;

import lissa.trading.tg.bot.tinkoff.dto.Stock;
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
