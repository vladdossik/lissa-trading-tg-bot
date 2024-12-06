package lissa.trading.tg.bot.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgPulseNewsResponseDto {
    private List<String> tickers;
    private String nickname;
    private String text;
    private String inserted;
    private String url;
}
