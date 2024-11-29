package lissa.trading.tg.bot.dto.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsSourceResponseDto {
    private String source;
    private NewsResponseDto news;
}
