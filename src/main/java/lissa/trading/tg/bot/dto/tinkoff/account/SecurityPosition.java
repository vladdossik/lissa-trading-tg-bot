package lissa.trading.tg.bot.dto.tinkoff.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityPosition {
    private String figi;
    private long blocked;
    private long balance;
}
