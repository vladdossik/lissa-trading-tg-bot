package lissa.trading.tg.bot.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgPulseBrandInfoResponseDto {
    private String name;
    private List<String> tickers;
    private String brandInfo;
    private String sector;
    private String country;
    private String main;
}
