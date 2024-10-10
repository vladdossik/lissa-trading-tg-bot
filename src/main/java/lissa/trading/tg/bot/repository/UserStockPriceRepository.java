package lissa.trading.tg.bot.repository;

import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.model.UserStockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStockPriceRepository extends JpaRepository<UserStockPrice, Long> {
    Optional<UserStockPrice> findByUserAndTicker(UserEntity user, String ticker);

    Optional<UserStockPrice> findByUserAndFigi(UserEntity user, String figi);
}
