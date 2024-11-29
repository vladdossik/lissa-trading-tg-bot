package lissa.trading.tg.bot.dto.tinkoffPulse.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsContentDto {
    private String type;
    private List<NewsTickerDto> instruments;
    private String text;
}
