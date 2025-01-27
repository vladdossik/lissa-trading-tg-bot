package lissa.trading.tg.bot.dto.tinkoff.account;

import lissa.trading.tg.bot.dto.tinkoff.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
    private List<UserAccount> userAccounts;
}
