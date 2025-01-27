package lissa.trading.tg.bot.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFavoriteStocksUpdateDto {
    private UUID externalId;
    private List<NotificationFavouriteStockDto> favoriteStocks;
}