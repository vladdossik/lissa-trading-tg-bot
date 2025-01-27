package lissa.trading.tg.bot.dto.tinkoff.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FigiesDto {
    private List<String> figies;
}
