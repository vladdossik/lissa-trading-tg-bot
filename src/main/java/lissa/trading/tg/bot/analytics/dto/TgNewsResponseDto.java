package lissa.trading.tg.bot.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgNewsResponseDto {
    private String title;
    private String pubDate;
    private String url;
}
