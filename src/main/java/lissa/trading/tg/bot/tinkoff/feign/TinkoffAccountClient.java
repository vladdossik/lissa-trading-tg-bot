package lissa.trading.tg.bot.tinkoff.feign;

import lissa.trading.lissa.auth.lib.dto.UpdateTinkoffTokenResponce;
import lissa.trading.tg.bot.tinkoff.dto.Stock;
import lissa.trading.tg.bot.tinkoff.dto.account.AccountInfoDto;
import lissa.trading.tg.bot.tinkoff.dto.account.BalanceDto;
import lissa.trading.tg.bot.tinkoff.dto.account.FavouriteStocksDto;
import lissa.trading.tg.bot.tinkoff.dto.account.MarginAttributesDto;
import lissa.trading.tg.bot.tinkoff.dto.account.SecurityPositionsDto;
import lissa.trading.tg.bot.tinkoff.dto.account.TinkoffTokenDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.FigiesDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.StocksDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.StocksPricesDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.TickersDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tinkoff-service", url = "${integration.rest.tinkoff-api-service-url}/v1/internal")
public interface TinkoffAccountClient {

    @PostMapping("/set-token")
    UpdateTinkoffTokenResponce setTinkoffToken(@RequestBody TinkoffTokenDto tinkoffToken);

    @GetMapping("/{ticker}")
    Stock getStockByTicker(@PathVariable("ticker") String ticker);

    @PostMapping("/getStocksByTickers")
    StocksDto getStocksByTickers(@RequestBody TickersDto tickers);

    @PostMapping("/prices")
    StocksPricesDto getPricesStocksByFigies(@RequestBody FigiesDto figiesDto);

    @GetMapping("/accounts")
    AccountInfoDto getAccountsInfo();

    @GetMapping("/favourites")
    FavouriteStocksDto getFavouriteStocks();

    @GetMapping("/portfolio/{accountId}")
    BalanceDto getPortfolio(@PathVariable("accountId") String accountId);

    @GetMapping("/margin/{accountId}")
    MarginAttributesDto getMarginAttributes(@PathVariable("accountId") String accountId);

    @GetMapping("/positions/{accountId}")
    SecurityPositionsDto getPositionsById(@PathVariable("accountId") String accountId);
}