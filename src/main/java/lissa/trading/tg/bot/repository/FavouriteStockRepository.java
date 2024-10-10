package lissa.trading.tg.bot.repository;

import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavouriteStockRepository extends JpaRepository<FavouriteStock, Long> {
    List<FavouriteStock> findByUser(UserEntity user);

    void deleteAllByUser(UserEntity user);
}
