package lissa.trading.tg.bot.config;

import com.github.benmanes.caffeine.cache.Cache;
import lissa.trading.tg.bot.bot.TelegramBot;
import lissa.trading.tg.bot.bot.UserState;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${bot.name}") String botName,
                                   @Value("${bot.token}") String botToken,
                                   UserService userService, FavouriteStockRepository favouriteStockRepository,
                                   @Qualifier("userStateCache") Cache<Long, UserState> userStates,
                                   @Qualifier("userEntityCache") Cache<Long, UserEntity> userEntities,
                                   @Qualifier("favouriteStockCache") Cache<Long, List<FavouriteStock>> favouriteStockCache) {
        return new TelegramBot(botName, botToken, userService, favouriteStockRepository, userStates, userEntities, favouriteStockCache);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
        return telegramBotsApi;
    }
}