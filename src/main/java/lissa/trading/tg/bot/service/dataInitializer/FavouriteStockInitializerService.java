package lissa.trading.tg.bot.service.dataInitializer;

import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class FavouriteStockInitializerService implements DataInitializerService {

    private final FavouriteStockRepository stockRepository;
    private final UserRepository userRepository;
    private final EasyRandom easyRandom;

    @Override
    public void createData() {
        if (stockRepository.count() == 0) {
            for (int i = 0; i < 10; i++) {
                FavouriteStock favouriteStock = easyRandom.nextObject(FavouriteStock.class);
                favouriteStock.setUser(userRepository.findById((long) (i + 1)).get());
                stockRepository.save(favouriteStock);
            }
            log.info("Favourite stock successfully initialized");
        } else {
            log.info("Favourite stock already initialized");
        }
    }
}
