package lissa.trading.tg.bot;

import lissa.trading.tg.bot.config.TelegramBotNotificationProperties;
import lissa.trading.tg.bot.notification.NotificationProducer;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.repository.UserStockPriceRepository;
import lissa.trading.tg.bot.service.UserProcessingService;
import lissa.trading.tg.bot.service.UserService;
import lissa.trading.tg.bot.tinkoff.feign.TinkoffAccountClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class BaseTest {

    @Mock
    protected UserService userService;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserProcessingService userProcessingService;

    @Mock
    protected TelegramBotNotificationProperties notificationProperties;

    @Mock
    protected UserStockPriceRepository userStockPriceRepository;

    @Mock
    protected TinkoffAccountClient tinkoffAccountClient;

    @Mock
    protected NotificationProducer notificationProducer;

    @Mock
    protected FavouriteStockRepository favouriteStockRepository;

    @Mock
    protected RoleRepository roleRepository;

    @Mock
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}