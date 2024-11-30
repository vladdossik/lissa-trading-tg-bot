package lissa.trading.tg.bot.dto.tinkoffPulse.ideas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerIdeaDto {
    private String id;
    private String name;
    private Double accuracy;
}