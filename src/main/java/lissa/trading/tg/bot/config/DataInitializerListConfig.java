package lissa.trading.tg.bot.config;

import lissa.trading.tg.bot.service.dataInitializer.DataInitializerService;
import lissa.trading.tg.bot.service.dataInitializer.FavouriteStockInitializerService;
import lissa.trading.tg.bot.service.dataInitializer.UserInitializerService;
import lissa.trading.tg.bot.service.dataInitializer.UserStockPriceInitializerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Profile("local")
public class DataInitializerListConfig {

    private final UserInitializerService userInitializerService;
    private final UserStockPriceInitializerService userStockPriceInitializerService;
    private final FavouriteStockInitializerService favouriteStockInitializerService;

    @Bean
    public List<DataInitializerService> dataInitializers() {
        return List.of(userInitializerService,
                favouriteStockInitializerService,
                userStockPriceInitializerService);
    }
}
