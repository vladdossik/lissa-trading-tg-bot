package lissa.trading.tg.bot.dto.tinkoffPulse.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockNewsDto {
    private String id;
    private String inserted;
    private NewsOwnerDto owner;
    private NewsContentDto content;
    private String url;
}