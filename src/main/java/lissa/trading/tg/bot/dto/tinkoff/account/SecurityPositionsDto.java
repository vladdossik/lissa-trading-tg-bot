package lissa.trading.tg.bot.dto.tinkoff.account;

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
