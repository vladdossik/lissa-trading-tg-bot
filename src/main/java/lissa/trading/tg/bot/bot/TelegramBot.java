package lissa.trading.tg.bot.bot;

import com.github.benmanes.caffeine.cache.Cache;
import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.notification.NotificationMessage;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final UserService userService;
    private final FavouriteStockRepository favouriteStockRepository;

    private final Cache<Long, UserState> userStates;
    private final Cache<Long, UserEntity> userEntities;
    private final Cache<Long, List<FavouriteStock>> favouriteStockCache;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public TelegramBot(
            @Value("${bot.name}") String botName,
            @Value("${bot.token}") String botToken,
            UserService userService, FavouriteStockRepository favouriteStockRepository,
            @Qualifier("userStateCache") Cache<Long, UserState> userStateCache,
            @Qualifier("userEntityCache") Cache<Long, UserEntity> userEntityCache,
            @Qualifier("favouriteStockCache") Cache<Long, List<FavouriteStock>> favouriteStockCache
    ) {
        super(botToken);
        this.botName = botName;
        this.userService = userService;
        this.favouriteStockRepository = favouriteStockRepository;
        this.userStates = userStateCache;
        this.userEntities = userEntityCache;
        this.favouriteStockCache = favouriteStockCache;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        CompletableFuture.runAsync(() -> handleUpdate(update), executorService)
                .exceptionally(ex -> {
                    log.error("Error processing update: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    public void sendNotification(NotificationMessage message) {
        String stockLink = String.format("https://www.tbank.ru/invest/stocks/%s/", message.getStockName());

        String formattedText = String.format(
                "Акция <a href=\"%s\">%s</a> изменилась в цене на %.2f%%. Текущая цена: %.3f %s",
                stockLink,
                message.getStockName(),
                message.getChangePercentage(),
                message.getCurrentPrice(),
                message.getCurrency()
        );

        sendMessage(message.getChatId(), formattedText, "HTML", true);
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleIncomingMessage(update);
        }
    }

    private void handleIncomingMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        BotCommand command = BotCommand.fromValue(messageText);
        if (command != BotCommand.UNKNOWN) {
            handleCommand(chatId, command, update);
        } else {
            handleUserState(chatId, messageText, update);
        }
    }

    private void handleCommand(Long chatId, BotCommand command, Update update) {
        switch (command) {
            case START -> processStartCommand(chatId, update);
            case TOKEN -> processTokenCommand(chatId);
            case INFO -> processInfoCommand(chatId, update);
            case FAVOURITES -> processFavouritesCommand(chatId);
            default -> sendMessage(chatId, "Неизвестная команда.");
        }
    }

    private void processStartCommand(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> sendMessage(chatId, "Вы уже зарегистрированы."),
                () -> promptForTinkoffToken(chatId)
        );
    }

    private void processTokenCommand(Long chatId) {
        sendMessage(chatId, "Пожалуйста, предоставьте ваш новый Tinkoff токен.");
        userStates.put(chatId, UserState.WAITING_FOR_NEW_TOKEN);
    }

    private void processInfoCommand(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> sendMessage(chatId, formatUserInfo(user)),
                () -> sendMessage(chatId, "Вы не зарегистрированы. Пожалуйста, начните с команды /start.")
        );
    }

    private void processFavouritesCommand(Long chatId) {
        List<FavouriteStock> favourites = getFavouritesFromCacheOrDB(chatId);

        if (favourites.isEmpty()) {
            sendMessage(chatId, "У вас пока нет избранных акций.");
        } else {
            String messageText = formatFavouritesList(favourites);
            sendMessage(chatId, messageText);
        }
    }

    private void handleUserState(Long chatId, String messageText, Update update) {
        UserState state = userStates.getIfPresent(chatId);
        if (state != null) {
            switch (state) {
                case TOKEN -> processTokenInput(chatId, messageText, update);
                case PASSWORD -> processPasswordInput(chatId, messageText);
                case WAITING_FOR_NEW_TOKEN -> processNewTokenInput(chatId, messageText, update);
                default -> promptForCommand(chatId);
            }
        } else {
            promptForCommand(chatId);
        }
    }

    private void processTokenInput(Long chatId, String token, Update update) {
        if (isValidToken(token)) {
            UserEntity user = createUserEntityWithToken(token, update);
            userEntities.put(chatId, user);

            sendMessage(chatId, "Ваш Tinkoff токен сохранен. Теперь введите пароль.");
            userStates.put(chatId, UserState.PASSWORD);
        } else {
            sendMessage(chatId, "Некорректный Tinkoff токен. Он должен начинаться с 't.' и иметь длину 88 символов.");
        }
    }

    private void processPasswordInput(Long chatId, String password) {
        if (isValidPassword(password)) {
            UserEntity user = userEntities.getIfPresent(chatId);

            if (user == null) {
                finalizeRegistration(chatId, "Сессия истекла. Начните регистрацию заново.");
                return;
            }

            user.setPassword(EncryptionService.encrypt(password));
            SignupRequest signupRequest = buildSignupRequest(user);

            handleUserRegistration(chatId, signupRequest);
        } else {
            sendMessage(chatId, "Пароль должен содержать минимум 3 символа. Попробуйте снова.");
        }
    }

    private void processNewTokenInput(Long chatId, String newToken, Update update) {
        if (isValidToken(newToken)) {
            String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

            userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                    user -> {
                        userService.updateUserToken(user.getTelegramNickname(), EncryptionService.encrypt(newToken));
                        sendMessage(chatId, "Ваш Tinkoff токен обновлен.");
                        clearUserSession(chatId);
                    },
                    () -> sendMessage(chatId, "Пользователь не найден. Пожалуйста, зарегистрируйтесь сначала.")
            );
        } else {
            sendMessage(chatId, "Некорректный Tinkoff токен. Пожалуйста, предоставьте корректный токен.");
        }
    }

    private void promptForTinkoffToken(Long chatId) {
        String messageText = "Пожалуйста, предоставьте ваш <a href=\"https://www.tbank.ru/invest/settings/api/\">Tinkoff токен</a>.";
        sendMessage(chatId, messageText, "HTML", true);
        userStates.put(chatId, UserState.TOKEN);
    }

    private void promptForCommand(Long chatId) {
        sendMessage(chatId, "Пожалуйста, введите команду или /start для начала.");
    }

    private void finalizeRegistration(Long chatId, String message) {
        sendMessage(chatId, message);
        clearUserSession(chatId);
    }

    private void clearUserSession(Long chatId) {
        userStates.invalidate(chatId);
        userEntities.invalidate(chatId);
        favouriteStockCache.invalidate(chatId);
    }

    private void handleUserRegistration(Long chatId, SignupRequest signupRequest) {
        UserRegistrationResponse response = userService.registerUser(signupRequest);
        if (response.isSuccess()) {
            userService.updateUserChatId(signupRequest.getTelegramNickname(), chatId);
            finalizeRegistration(chatId, "Регистрация завершена.");
        } else {
            sendMessage(chatId, response.getMessage());
        }
    }

    private List<FavouriteStock> getFavouritesFromCacheOrDB(Long chatId) {
        List<FavouriteStock> favourites = favouriteStockCache.getIfPresent(chatId);

        if (favourites == null) {
            favourites = fetchFavouritesFromDB(chatId);
            if (favourites != null) {
                favouriteStockCache.put(chatId, favourites);
            }
        }

        return favourites != null ? favourites : Collections.emptyList();
    }

    private List<FavouriteStock> fetchFavouritesFromDB(Long chatId) {
        Optional<UserEntity> optionalUser = userService.getUserByChatId(chatId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            return favouriteStockRepository.findByUser(user);
        } else {
            sendMessage(chatId, "Вы не зарегистрированы. Пожалуйста, начните с команды /start.");
            return Collections.emptyList();
        }
    }

    private String formatFavouritesList(List<FavouriteStock> favourites) {
        String tickers = favourites.stream()
                .map(FavouriteStock::getTicker)
                .filter(ticker -> ticker != null && !ticker.isEmpty())
                .collect(Collectors.joining(", "));
        return "Ваши избранные акции:\n" + tickers;
    }

    private UserEntity createUserEntityWithToken(String token, Update update) {
        UserEntity user = new UserEntity();
        user.setTinkoffToken(EncryptionService.encrypt(token));
        registerUserDetails(user, update);
        return user;
    }

    private SignupRequest buildSignupRequest(UserEntity user) {
        return SignupRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .telegramNickname(user.getTelegramNickname())
                .tinkoffToken(user.getTinkoffToken())
                .password(user.getPassword())
                .role(Set.of("USER"))
                .build();
    }

    private void registerUserDetails(UserEntity user, Update update) {
        user.setTelegramNickname(getSafeValue(update.getMessage().getFrom().getUserName()));
        user.setFirstName(getSafeValue(update.getMessage().getFrom().getFirstName()));
        user.setLastName(getSafeValue(update.getMessage().getFrom().getLastName()));
    }

    private String formatUserInfo(UserInfoDto userInfo) {
        return """
                Информация о пользователе:
                -------------------------
                ID: %s
                Никнейм: %s
                """.formatted(
                userInfo.getExternalId(),
                getSafeValue(userInfo.getTelegramNickname())
        );
    }

    private boolean isValidToken(String token) {
        return token != null && token.startsWith("t.") && token.length() == 88;
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 3;
    }

    private void sendMessage(Long chatId, String text, String parseMode, boolean disableWebPagePreview) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode(parseMode)
                .disableWebPagePreview(disableWebPagePreview)
                .build();
        executeMessage(message);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "Не указано";
    }
}
