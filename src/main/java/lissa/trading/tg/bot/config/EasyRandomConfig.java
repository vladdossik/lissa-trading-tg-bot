package lissa.trading.tg.bot.config;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class EasyRandomConfig {

    @Bean
    public EasyRandom easyRandom() {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .stringLengthRange(5, 10)
                .collectionSizeRange(1, 3);
        return new EasyRandom(parameters);
    }
}
