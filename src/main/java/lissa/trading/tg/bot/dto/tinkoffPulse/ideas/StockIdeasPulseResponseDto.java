package lissa.trading.tg.bot.dto.tinkoffPulse.ideas;

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
public class StockIdeasPulseResponseDto extends PulseResponseDto {
    private List<StockIdeaDto> ideas;
}

