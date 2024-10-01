package lissa.trading.tg.bot.tinkoff.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TickersDto {
    private List<String> tickers;
}
