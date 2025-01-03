package lissa.trading.tg.bot.service.dataInitializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MainDataInitializer {

    private final List<DataInitializerService> dataInitializerServices;

    @PostConstruct
    public void init() {
        dataInitializerServices.forEach(DataInitializerService::createData);
    }
}
