package lissa.trading.tg.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // минимальное количество потоков
        executor.setMaxPoolSize(20); // максимальное количество потоков
        executor.setQueueCapacity(500); // размер очереди
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}