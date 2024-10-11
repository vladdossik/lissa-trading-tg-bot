package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.config.TelegramBotNotificationProperties;
import lissa.trading.tg.bot.exception.RetrieveFailedException;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.model.UserStockPrice;
import lissa.trading.tg.bot.notification.NotificationMessage;
import lissa.trading.tg.bot.notification.NotificationProducer;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.repository.UserStockPriceRepository;
import lissa.trading.tg.bot.tinkoff.dto.account.TinkoffTokenDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.FigiesDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.StockPrice;
import lissa.trading.tg.bot.tinkoff.dto.stock.StocksPricesDto;
import lissa.trading.tg.bot.tinkoff.feign.TinkoffAccountClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProcessingService {

    private static final Set<String> SUPPORTED_INSTRUMENT_TYPES = Set.of("share", "bond");

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserStockPriceRepository userStockPriceRepository;
    private final TinkoffAccountClient tinkoffAccountClient;
    private final TelegramBotNotificationProperties notificationProperties;
    private final NotificationProducer notificationProducer;

    // --- Асинхронная обработка пользователя ---
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> processUserAsync(Long userId) {
        try {
            processUser(userId);
        } catch (Exception e) {
            log.error("Error processing user with ID {}: {}", userId, e.getMessage(), e);
            throw new RetrieveFailedException("Failed to process user with ID " + userId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // --- Основная логика обработки пользователя ---
    private void processUser(Long userId) {
        log.debug("Processing user with ID {}", userId);

        UserEntity user = fetchUserWithFavouriteStocks(userId);
        setTinkoffTokenForClient(user.getTinkoffToken());

        userService.updateUserFromTinkoffData(user);

        Set<FavouriteStock> favouriteStocks = user.getFavouriteStocks();
        log.debug("User {} has {} favourite stocks", user.getTelegramNickname(), favouriteStocks.size());

        if (favouriteStocks.isEmpty()) {
            log.debug("User {} has no favourite stocks, skipping", user.getTelegramNickname());
            return;
        }

        List<FavouriteStock> supportedStocks = filterSupportedFavouriteStocks(favouriteStocks);
        if (supportedStocks.isEmpty()) {
            log.debug("User {} has no supported favourite stocks, skipping", user.getTelegramNickname());
            return;
        }

        Map<String, Double> figiToPriceMap = fetchCurrentPrices(supportedStocks, user);
        if (figiToPriceMap.isEmpty()) {
            log.debug("No valid prices for user {}", user.getTelegramNickname());
            return;
        }

        supportedStocks.forEach(stock -> processFavouriteStock(user, stock, figiToPriceMap));
    }

    // --- Получение и подготовка данных пользователя ---
    private UserEntity fetchUserWithFavouriteStocks(Long userId) {
        return userRepository.findWithFavouriteStocksById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
    }

    private void setTinkoffTokenForClient(String encryptedToken) {
        String decryptedToken = EncryptionService.decrypt(encryptedToken);
        tinkoffAccountClient.setTinkoffToken(new TinkoffTokenDto(decryptedToken));
        log.debug("Successfully decrypted token for user");
    }

    // --- Фильтрация избранных акций ---
    private List<FavouriteStock> filterSupportedFavouriteStocks(Set<FavouriteStock> favouriteStocks) {
        return favouriteStocks.stream()
                .filter(stock -> SUPPORTED_INSTRUMENT_TYPES.contains(stock.getInstrumentType()))
                .toList();
    }

    // --- Работа с ценами акций ---
    private Map<String, Double> fetchCurrentPrices(List<FavouriteStock> stocks, UserEntity user) {
        List<String> figies = stocks.stream()
                .map(FavouriteStock::getFigi)
                .filter(Objects::nonNull)
                .toList();

        if (figies.isEmpty()) {
            log.debug("No valid figies for user {}", user.getTelegramNickname());
            return Collections.emptyMap();
        }

        log.debug("Fetching prices for figies: {}", figies);
        StocksPricesDto pricesDto = tinkoffAccountClient.getPricesStocksByFigies(new FigiesDto(figies));

        return pricesDto.getPrices().stream()
                .filter(stockPrice -> stockPrice.getFigi() != null && stockPrice.getPrice() != null)
                .collect(Collectors.toMap(
                        StockPrice::getFigi,
                        StockPrice::getPrice,
                        (existing, replacement) -> existing
                ));
    }

    // --- Обработка избранных акций пользователя ---
    private void processFavouriteStock(UserEntity user, FavouriteStock stock, Map<String, Double> figiToPriceMap) {
        String figi = stock.getFigi();
        Double currentPrice = figiToPriceMap.get(figi);

        if (currentPrice == null) {
            log.debug("Price not found for figi {} for user {}", figi, user.getTelegramNickname());
            return;
        }

        log.debug("Checking price change for stock {} (figi: {})", stock.getServiceTicker(), figi);

        Optional<UserStockPrice> optionalPrice = userStockPriceRepository.findByUserAndFigi(user, figi);

        if (optionalPrice.isPresent()) {
            updateExistingStockPrice(user, stock, currentPrice, optionalPrice.get());
        } else {
            saveNewStockPrice(user, stock, currentPrice);
        }
    }

    // --- Обновление существующих записей о ценах акций ---
    private void updateExistingStockPrice(UserEntity user, FavouriteStock stock, Double currentPrice, UserStockPrice existingPrice) {
        if (shouldUpdatePriceTimestamp(existingPrice.getPriceTimestamp())) {
            updatePriceTimestamp(existingPrice, currentPrice);
            return;
        }

        double changePercentage = calculatePriceChangePercentage(existingPrice, currentPrice);

        if (Double.isNaN(changePercentage)) {
            log.debug("Price change calculation not possible for figi {}", stock.getFigi());
            return;
        }

        if (Math.abs(changePercentage) >= notificationProperties.getCriticalPriceChangePercentage()) {
            log.info("Price change exceeds threshold for {}, notifying user {}", stock.getFigi(), user.getTelegramNickname());
            sendNotification(user, stock, currentPrice, changePercentage);
            updatePriceDataAfterNotification(existingPrice, currentPrice);
        }
    }

    // --- Логика обновления временных меток цен ---
    private boolean shouldUpdatePriceTimestamp(OffsetDateTime previousTimestamp) {
        return previousTimestamp.isBefore(OffsetDateTime.now().minusMinutes(notificationProperties.getPriceChangeWindowMinutes()));
    }

    private void updatePriceTimestamp(UserStockPrice existingPrice, Double currentPrice) {
        existingPrice.setLastPrice(currentPrice);
        existingPrice.setPriceTimestamp(OffsetDateTime.now());
        userStockPriceRepository.save(existingPrice);
    }

    // --- Вычисление процента изменения цены ---
    private double calculatePriceChangePercentage(UserStockPrice existingPrice, Double currentPrice) {
        Double lastNotifiedPrice = existingPrice.getLastNotifiedPrice();
        Double previousPrice = existingPrice.getLastPrice();

        if (lastNotifiedPrice != null && lastNotifiedPrice != 0.0) {
            return ((currentPrice - lastNotifiedPrice) / lastNotifiedPrice) * 100;
        } else if (previousPrice != 0.0) {
            return ((currentPrice - previousPrice) / previousPrice) * 100;
        } else {
            return Double.NaN;
        }
    }

    // --- Отправка уведомлений пользователю ---
    private void sendNotification(UserEntity user, FavouriteStock stock, Double currentPrice, double changePercentage) {
        NotificationMessage message = new NotificationMessage(
                user.getTelegramChatId(),
                stock.getServiceTicker() != null ? stock.getServiceTicker() : stock.getFigi(),
                currentPrice,
                changePercentage,
                stock.getCurrency().name()
        );

        // Используем NotificationProducer для отправки сообщения
        notificationProducer.sendNotification(message);
        log.debug("Sent notification to user {} about instrument {}", user.getTelegramNickname(), stock.getFigi());
    }

    // --- Обновление данных после отправки уведомления ---
    private void updatePriceDataAfterNotification(UserStockPrice existingPrice, Double currentPrice) {
        existingPrice.setLastPrice(currentPrice);
        existingPrice.setPriceTimestamp(OffsetDateTime.now());
        existingPrice.setLastNotifiedPrice(currentPrice);
        userStockPriceRepository.save(existingPrice);
    }

    // --- Сохранение новых записей о ценах акций ---
    private void saveNewStockPrice(UserEntity user, FavouriteStock stock, Double currentPrice) {
        UserStockPrice newUserStockPrice = new UserStockPrice(
                null,
                user,
                stock.getFigi(),
                currentPrice,
                OffsetDateTime.now(),
                stock.getServiceTicker(),
                stock.getName(),
                stock.getInstrumentType(),
                currentPrice,
                stock.getCurrency()
        );
        userStockPriceRepository.save(newUserStockPrice);
        log.debug("Saved new price record for {} (figi: {})", stock.getServiceTicker(), stock.getFigi());
    }
}