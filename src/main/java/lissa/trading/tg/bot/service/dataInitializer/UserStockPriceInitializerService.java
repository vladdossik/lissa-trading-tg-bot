package lissa.trading.tg.bot.service.dataInitializer;

import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.model.UserStockPrice;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.repository.UserStockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Order(2)
@Profile("local")
public class UserStockPriceInitializerService implements DataInitializerService {

    private final UserStockPriceRepository userStockPriceRepository;
    private final UserRepository userRepository;
    private final EasyRandom easyRandom;

    @Override
    public void createData() {
        if (userStockPriceRepository.count() == 0) {
            for (int i = 0; i < 10; i++) {
                UserStockPrice userStockPrice = easyRandom.nextObject(UserStockPrice.class);
                userStockPrice.setUser(userRepository.findById((long) (i + 1)).get());
                userStockPriceRepository.save(userStockPrice);
            }
            log.info("UserStockPrice data successfully initialized");
        } else {
            log.info("UserStockPrice data already initialized");
        }
    }
}
