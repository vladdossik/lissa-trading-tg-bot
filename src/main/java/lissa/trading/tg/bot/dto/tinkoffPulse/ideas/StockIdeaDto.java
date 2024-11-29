package lissa.trading.tg.bot.dto.tinkoffPulse.ideas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockIdeaDto {
    private String id;
    private String title;
    private BrokerIdeaDto broker;
    private List<TickerIdeaDto> tickers;
    private Double yield;
    private String url;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("price_start")
    private Double priceStart;

    @JsonProperty("price")
    private Double actualPrice;

    @JsonProperty("target_yield")
    private Double targetYield;
}
