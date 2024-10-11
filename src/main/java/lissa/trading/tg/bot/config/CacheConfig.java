package lissa.trading.tg.bot.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lissa.trading.tg.bot.bot.UserState;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.caffeine.expire-after-write}")
    private int expireAfterWrite;

    @Value("${cache.caffeine.maximum-size}")
    private int maximumSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users", "userStates", "userEntities");
        cacheManager.setCaffeine(caffeineConfig());
        return cacheManager;
    }

    @Bean(name = "userStateCache")
    public Cache<Long, UserState> userStateCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
    }

    @Bean(name = "userEntityCache")
    public Cache<Long, UserEntity> userEntityCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    @Bean(name = "favouriteStockCache")
    public Cache<Long, List<FavouriteStock>> favouriteStockCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maximumSize);
    }
}
