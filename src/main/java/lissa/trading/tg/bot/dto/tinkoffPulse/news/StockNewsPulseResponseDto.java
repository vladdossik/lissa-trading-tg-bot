package lissa.trading.tg.bot.dto.tinkoffPulse.news;

import lissa.trading.tg.bot.dto.tinkoffPulse.PulseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockNewsPulseResponseDto extends PulseResponseDto {
    private List<StockNewsDto> items;
}