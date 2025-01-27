package lissa.trading.tg.bot.mapper;

import lissa.trading.tg.bot.dto.notification.NotificationFavouriteStockDto;
import lissa.trading.tg.bot.model.FavouriteStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FavouriteStockMapper {
    FavouriteStock toFavoriteStockFromNotificationFavoriteStockDto(
            NotificationFavouriteStockDto favouriteStocksDto);

    List<FavouriteStock> toFavoriteStocksFromNotificationFavoriteStockDto(
            List<NotificationFavouriteStockDto> favouriteStockDtoList);


    NotificationFavouriteStockDto toNotificationFavouriteStockDto(
            FavouriteStock favoriteStocksEntity);

    List<NotificationFavouriteStockDto> toNotificationFavouriteStockDtoList(
            List<FavouriteStock> favoriteStocksEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "serviceTicker", ignore = true)
    void updateFavoriteStocksFromFavoriteStock(FavouriteStock favoriteStocksEntity,
                                               @MappingTarget FavouriteStock targetFavoriteStocksEntity);
}
