package lissa.trading.tg.bot.feign;

import lissa.trading.tg.bot.dto.tinkoff.stock.StocksPricesDto;
import lissa.trading.tg.bot.dto.tinkoff.stock.TickersDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${integration.rest.user-service.url}/v1/internal")
public interface UserServiceClient {

    @GetMapping("/update/favouriteStocksPrices/{externalId}")
    StocksPricesDto getUpdatedFavouriteStocksPrices(@PathVariable UUID externalId);

    @PostMapping("/update/favouriteStocks/{externalId}")
    ResponseEntity<String> updateUserFavoriteStocks(@PathVariable UUID externalId, @RequestBody TickersDto tickersDto);
}
