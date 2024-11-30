package lissa.trading.tg.bot.analytics.dto;

import lissa.trading.tg.bot.analytics.AnalyticsInfoType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsRequestDto {
    private AnalyticsInfoType type;
    private Long chatId;
    private List<String> tickers;
}