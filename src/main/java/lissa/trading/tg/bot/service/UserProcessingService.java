package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.exception.RetrieveFailedException;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.model.UserStockPrice;
import lissa.trading.tg.bot.notification.NotificationMessage;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.repository.UserStockPriceRepository;
import lissa.trading.tg.bot.tinkoff.dto.account.TinkoffTokenDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.FigiesDto;
import lissa.trading.tg.bot.tinkoff.dto.stock.StockPrice;
import lissa.trading.tg.bot.tinkoff.dto.stock.StocksPricesDto;
import lissa.trading.tg.bot.tinkoff.feign.TinkoffAccountClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    private final RabbitTemplate rabbitTemplate;
    @Value("${telegram-bot.notification.priceChangeWindowMinutes}")
    private long priceChangeWindowMinutes;

    @Value("${telegram-bot.notification.criticalPriceChangePercentage}")
    private double criticalPriceChangePercentage;

    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> processUserAsync(Long userId) {
        try {
            processUser(userId);
        } catch (Exception e) {
            log.error("Ошибка при обработке пользователя с ID {}: {}", userId, e.getMessage(), e);
            throw new RetrieveFailedException("Failed to process user with ID " + userId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void processUser(Long userId) {
        log.debug("Обработка пользователя с ID {}", userId);

        UserEntity user = userRepository.findWithFavouriteStocksById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        String decryptedToken = EncryptionService.decrypt(user.getTinkoffToken());
        tinkoffAccountClient.setTinkoffToken(new TinkoffTokenDto(decryptedToken));

        log.debug("Успешно расшифрован токен для пользователя {}", user.getTelegramNickname());

        userService.updateUserFromTinkoffData(user);

        Set<FavouriteStock> favouriteStocks = user.getFavouriteStocks();
        log.debug("Пользователь {} имеет {} любимых акций", user.getTelegramNickname(), favouriteStocks.size());

        if (favouriteStocks.isEmpty()) {
            log.debug("Пользователь {} не имеет любимых акций, пропуск", user.getTelegramNickname());
            return;
        }

        List<FavouriteStock> supportedFavouriteStocks = favouriteStocks.stream()
                .filter(stock -> SUPPORTED_INSTRUMENT_TYPES.contains(stock.getInstrumentType()))
                .toList();

        if (supportedFavouriteStocks.isEmpty()) {
            log.debug("Пользователь {} не имеет поддерживаемых любимых акций, пропуск", user.getTelegramNickname());
            return;
        }

        List<String> figies = supportedFavouriteStocks.stream()
                .map(FavouriteStock::getFigi)
                .filter(Objects::nonNull)
                .toList();

        if (figies.isEmpty()) {
            log.debug("Нет валидных figies для пользователя {}", user.getTelegramNickname());
            return;
        }

        log.debug("Получение цен для figies: {}", figies);
        StocksPricesDto pricesDto = tinkoffAccountClient.getPricesStocksByFigies(new FigiesDto(figies));

        Map<String, Double> figiToPriceMap = pricesDto.getPrices().stream()
                .filter(stockPrice -> stockPrice.getFigi() != null && stockPrice.getPrice() != null)
                .collect(Collectors.toMap(
                        StockPrice::getFigi,
                        StockPrice::getPrice,
                        (existing, replacement) -> existing
                ));

        log.debug("Получены цены для пользователя {}: {}", user.getTelegramNickname(), figiToPriceMap);

        supportedFavouriteStocks.forEach(favouriteStock -> processFavouriteStock(user, favouriteStock, figiToPriceMap));
    }

    private void processFavouriteStock(UserEntity user, FavouriteStock favouriteStock, Map<String, Double> figiToPriceMap) {
        String figi = favouriteStock.getFigi();
        String instrumentName = favouriteStock.getName();
        String instrumentTicker = favouriteStock.getServiceTicker();

        Double currentPrice = figiToPriceMap.get(figi);
        if (currentPrice == null) {
            log.debug("Цена не найдена для figi {} у пользователя {}", figi, user.getTelegramNickname());
            return;
        }

        log.debug("Проверка изменения цены для акции {} (figi: {})",
                instrumentTicker != null ? instrumentTicker : figi, figi);

        Optional<UserStockPrice> optionalUserStockPrice = userStockPriceRepository.findByUserAndFigi(user, figi);

        if (optionalUserStockPrice.isPresent()) {
            UserStockPrice userStockPrice = optionalUserStockPrice.get();
            OffsetDateTime previousTimestamp = userStockPrice.getPriceTimestamp();
            Double previousPrice = userStockPrice.getLastPrice();
            Double lastNotifiedPrice = userStockPrice.getLastNotifiedPrice();

            log.debug("Предыдущая цена для {} (figi: {}): {}, время: {}, последняя уведомленная цена: {}",
                    instrumentTicker != null ? instrumentTicker : figi, figi, previousPrice, previousTimestamp, lastNotifiedPrice);

            if (previousTimestamp.isBefore(OffsetDateTime.now().minusMinutes(priceChangeWindowMinutes))) {
                log.debug("Прошло достаточно времени для обновления цены по figi {}, обновление данных", figi);
                userStockPrice.setLastPrice(currentPrice);
                userStockPrice.setPriceTimestamp(OffsetDateTime.now());
                userStockPrice.setCurrency(favouriteStock.getCurrency());
                userStockPriceRepository.save(userStockPrice);
                return;
            }

            double changePercentage;
            if (lastNotifiedPrice != null && lastNotifiedPrice != 0.0) {
                changePercentage = ((currentPrice - lastNotifiedPrice) / lastNotifiedPrice) * 100;
            } else if (previousPrice != 0.0) {
                changePercentage = ((currentPrice - previousPrice) / previousPrice) * 100;
            } else {
                changePercentage = Double.NaN;
            }

            log.debug("Изменение цены для {}: {}%", figi, changePercentage);

            if (Double.isNaN(changePercentage)) {
                log.debug("Изменение цены для {} невозможно из-за предыдущей цены 0.", figi);
                return;
            }

            if (Math.abs(changePercentage) >= criticalPriceChangePercentage) {
                log.info("Изменение цены превышает порог для {}, уведомление пользователя {}", figi, user.getTelegramNickname());

                NotificationMessage message = new NotificationMessage(
                        user.getTelegramChatId(),
                        instrumentTicker != null ? instrumentTicker : figi,
                        currentPrice,
                        changePercentage,
                        favouriteStock.getCurrency().name()
                );

                // Отправка уведомления через RabbitMQ
                rabbitTemplate.convertAndSend("notificationQueue", message);
                log.debug("Отправлено уведомление пользователю {} о инструменте {}", user.getTelegramNickname(), figi);

                userStockPrice.setLastPrice(currentPrice);
                userStockPrice.setPriceTimestamp(OffsetDateTime.now());
                userStockPrice.setLastNotifiedPrice(currentPrice);
                userStockPrice.setCurrency(favouriteStock.getCurrency());
                userStockPriceRepository.save(userStockPrice);
            }
        } else {
            log.debug("Нет предыдущей цены для {}, сохранение новой записи", figi);
            UserStockPrice newUserStockPrice = new UserStockPrice(
                    null,
                    user,
                    figi,
                    currentPrice,
                    OffsetDateTime.now(),
                    instrumentTicker,
                    instrumentName,
                    favouriteStock.getInstrumentType(),
                    currentPrice, // lastNotifiedPrice
                    favouriteStock.getCurrency()
            );
            userStockPriceRepository.save(newUserStockPrice);
            log.debug("Сохранена новая запись цены для {} (figi: {})",
                    instrumentTicker != null ? instrumentTicker : figi, figi);
        }
    }
}