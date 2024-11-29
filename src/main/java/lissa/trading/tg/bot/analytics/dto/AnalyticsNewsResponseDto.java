package lissa.trading.tg.bot.analytics.dto;

import lissa.trading.tg.bot.analytics.AnalyticsInfoType;
import lissa.trading.tg.bot.dto.news.NewsSourceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsNewsResponseDto {
    private AnalyticsInfoType type;
    private Long chatId;
    private List<NewsSourceResponseDto> data;
}
