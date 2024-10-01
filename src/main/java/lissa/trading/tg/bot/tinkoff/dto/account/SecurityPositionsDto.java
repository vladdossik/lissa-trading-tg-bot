package lissa.trading.tg.bot.tinkoff.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityPositionsDto {
    private List<SecurityPosition> positions;
}
