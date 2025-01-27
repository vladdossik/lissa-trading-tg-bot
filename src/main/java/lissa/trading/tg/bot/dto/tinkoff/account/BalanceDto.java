package lissa.trading.tg.bot.dto.tinkoff.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDto {
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal totalAmountBalance;
}