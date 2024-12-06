package lissa.trading.tg.bot.analytics.dto;

import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsPulseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsNewsPulseResponseDto extends AnalyticsPulseResponseDto {
    private List<StockNewsPulseResponseDto> data;
}