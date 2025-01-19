package lissa.trading.tg.bot.feign;

import lissa.trading.lissa.auth.lib.dto.UpdateTinkoffTokenResponce;
import lissa.trading.tg.bot.dto.tinkoff.Stock;
import lissa.trading.tg.bot.dto.tinkoff.account.AccountInfoDto;
import lissa.trading.tg.bot.dto.tinkoff.account.BalanceDto;
import lissa.trading.tg.bot.dto.tinkoff.account.FavouriteStocksDto;
import lissa.trading.tg.bot.dto.tinkoff.account.MarginAttributesDto;
import lissa.trading.tg.bot.dto.tinkoff.account.SecurityPositionsDto;
import lissa.trading.tg.bot.dto.tinkoff.account.TinkoffTokenDto;
import lissa.trading.tg.bot.dto.tinkoff.stock.FigiesDto;
import lissa.trading.tg.bot.dto.tinkoff.stock.StocksDto;
import lissa.trading.tg.bot.dto.tinkoff.stock.StocksPricesDto;
import lissa.trading.tg.bot.dto.tinkoff.stock.TickersDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tinkoff-service", url = "${integration.rest.tinkoff-api-service.url}/v1/internal")
public interface TinkoffAccountClient {

    @PostMapping("/set-token")
    UpdateTinkoffTokenResponce setTinkoffToken(@RequestBody TinkoffTokenDto tinkoffToken);

    @GetMapping("/{ticker}")
    Stock getStockByTicker(@PathVariable("ticker") String ticker);

    @PostMapping("/getStocksByTickers")
    StocksDto getStocksByTickers(@RequestBody TickersDto tickers);

    @PostMapping("/prices")
    StocksPricesDto getPricesStocksByFigies(@RequestBody FigiesDto figiesDto);
