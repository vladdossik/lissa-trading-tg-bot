package lissa.trading.tg.bot.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgPulseIdeaResponseDto {
    private String title;
    private String name;
    private Double accuracy;
    private List<String> tickers;
    private Double priceStart;
    private Double actualPrice;
    private Double yield;
    private Double targetYield;
    private String dateStart;
    private String dateEnd;
    private String url;
}
