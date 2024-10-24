package lissa.trading.tg.bot;

import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.exception.RetrieveFailedException;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.model.UserStockPrice;
import lissa.trading.tg.bot.service.UserProcessingService;
import lissa.trading.tg.bot.tinkoff.dto.Currency;
import lissa.trading.tg.bot.tinkoff.dto.stock.FigiesDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.StockPrice;
import lissa.trading.tg.bot.tinkoff.dto.stock.StocksPricesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserProcessingServiceTest extends BaseTest {

    @InjectMocks
    private UserProcessingService userProcessingService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        userProcessingService = new UserProcessingService(
                userService,
                userRepository,
                userStockPriceRepository,
                tinkoffAccountClient,
                notificationProperties,
                notificationProducer
        );
    }

    @Test
    void testProcessUserAsync_Success() {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTelegramNickname("testUser");
        user.setTinkoffToken("encryptedToken");
        user.setFavouriteStocks(new HashSet<>());

        FavouriteStock stock1 = new FavouriteStock();
        stock1.setFigi("figi1");
        stock1.setServiceTicker("TICKER1");
        stock1.setInstrumentType("share");
        stock1.setCurrency(Currency.RUB);

        user.getFavouriteStocks().add(stock1);

        when(userRepository.findWithFavouriteStocksById(userId)).thenReturn(Optional.of(user));
        when(notificationProperties.getCriticalPriceChangePercentage()).thenReturn(5.0);
        when(notificationProperties.getPriceChangeWindowMinutes()).thenReturn(60L);

        when(tinkoffAccountClient.getPricesStocksByFigies(any(FigiesDto.class)))
                .thenReturn(new StocksPricesDto(List.of(new StockPrice("figi1", 100.0))));

        when(userStockPriceRepository.findByUserAndFigi(user, "figi1"))
                .thenReturn(Optional.empty());

        try (MockedStatic<EncryptionService> mockedEncryptionService = mockStatic(EncryptionService.class)) {
            mockedEncryptionService.when(() -> EncryptionService.decrypt("encryptedToken")).thenReturn("decryptedToken");

            userProcessingService.processUserAsync(userId).join();

            verify(userService).updateUserFromTinkoffData(user);
            verify(userStockPriceRepository).save(any(UserStockPrice.class));
            verifyNoInteractions(notificationProducer);
        }
    }

    @Test
    void testProcessUserAsync_Exception() {
        Long userId = 1L;
        when(userRepository.findWithFavouriteStocksById(userId)).thenReturn(Optional.empty());

        RetrieveFailedException exception = assertThrows(RetrieveFailedException.class, () -> {
            userProcessingService.processUserAsync(userId).join();
        });

        assertTrue(exception.getMessage().contains("Failed to process user with ID " + userId));
    }
}