package lissa.trading.tg.bot.dto.tinkoffPulse.ideas;

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
    private String dateStart;
    private String dateEnd;
    private Double priceStart;
    private Double actualPrice;
    private Double targetYield;
}