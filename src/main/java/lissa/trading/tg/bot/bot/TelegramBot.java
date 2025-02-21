package lissa.trading.tg.bot.bot;

import com.github.benmanes.caffeine.cache.Cache;
import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.analytics.AnalyticsInfoType;
import lissa.trading.tg.bot.analytics.dto.AnalyticsRequestDto;
import lissa.trading.tg.bot.analytics.service.AnalyticsRequestService;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.notification.NotificationMessage;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.service.UserProcessingService;
import lissa.trading.tg.bot.service.UserService;
import lissa.trading.tg.bot.utils.MessageConstants;
import lissa.trading.tg.bot.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
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
    private final UserProcessingService userProcessingService;
    private final FavouriteStockRepository favouriteStockRepository;
    private final AnalyticsRequestService requestService;

    private final Cache<Long, UserState> userStates;
    private final Cache<Long, UserEntity> userEntities;
    private final Cache<Long, List<FavouriteStock>> favouriteStockCache;
    private final Cache<Long, List<String>> stocksForInfoCache;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public TelegramBot(
            @Value("${bot.name}") String botName,
            @Value("${bot.token}") String botToken,
            UserService userService, UserProcessingService userProcessingService,
            FavouriteStockRepository favouriteStockRepository,
            AnalyticsRequestService requestService,
            @Qualifier("stocksForInfoCache") Cache<Long, List<String>> stocksForInfoCache,
            @Qualifier("userStateCache") Cache<Long, UserState> userStateCache,
            @Qualifier("userEntityCache") Cache<Long, UserEntity> userEntityCache,
            @Qualifier("favouriteStockCache") Cache<Long, List<FavouriteStock>> favouriteStockCache
    ) {
        super(botToken);
        this.botName = botName;
        this.userService = userService;
        this.userProcessingService = userProcessingService;
        this.favouriteStockRepository = favouriteStockRepository;
        this.requestService = requestService;
        this.stocksForInfoCache = stocksForInfoCache;
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

    public void printPulseResponse(List<String> messages, Long chatId) {
        for (String message : messages) {
            sendMessage(chatId, message, "HTML", true);
        }
        userStates.put(chatId, UserState.WAITING_FOR_NEXT_COMMAND);
        sendMessage(chatId, MessageConstants.CHOOSE_TYPE_MESSAGE);
    }

    public void printNewsResponse(List<String> messages, Long chatId) {
        for (String elem : messages) {
            sendMessage(chatId, elem, "HTML", true);
        }
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
            if (messageText.startsWith("/")) {
                sendMessage(chatId, "Неизвестная команда.");
            } else {
                handleUserState(chatId, messageText, update);
            }
        }
    }

    private void handleCommand(Long chatId, BotCommand command, Update update) {
        switch (command) {
            case START -> processStartCommand(chatId, update);
            case TOKEN -> processTokenCommand(chatId);
            case INFO -> processInfoCommand(chatId, update);
            case FAVOURITES -> processFavouritesCommand(chatId);
            case PULSE -> processTinkoffPulseCommand(chatId);
            case NEWS -> processNewsCommand(chatId);
            case CANCEL -> processCancelCommand(chatId);
            case REFRESH -> processRefreshCommand(chatId);
            case HELP -> processHelpCommand(chatId);
            case ADD_FAVOURITES -> processAddFavouritesCommand(chatId);
            case REMOVE_FAVOURITES -> processRemoveFavouritesCommand(chatId);
            default -> sendMessage(chatId, "Неизвестная команда.");
        }
    }

    private void processStartCommand(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> {
                    sendMessage(chatId, "Вы уже зарегистрированы.");
                    userService.updateUserChatId(telegramNickname, chatId);
                    },
                () -> promptForTinkoffToken(chatId)
        );
    }

    private void processTokenCommand(Long chatId) {
        sendMessage(chatId, "Пожалуйста, предоставьте ваш новый Tinkoff токен. Обновлять токен можно раз в 5 минут. Вы можете отменить операцию в любое время, введя команду /cancel.");
        userStates.put(chatId, UserState.WAITING_FOR_NEW_TOKEN);
    }

    private void processInfoCommand(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> sendMessage(chatId, formatUserInfo(user)),
                () -> sendNotRegisteredMessage(chatId)
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

    private void processTinkoffPulseCommand(Long chatId) {
        sendMessage(chatId, MessageConstants.PRINT_TICKERS_MESSAGE);
        stocksForInfoCache.invalidate(chatId);
        userStates.put(chatId, UserState.WAITING_FOR_PULSE_TICKERS);
    }

    private void processNewsCommand(Long chatId) {
        sendMessage(chatId, MessageConstants.PRINT_TICKERS_MESSAGE);
        userStates.put(chatId, UserState.WAITING_FOR_NEWS_TICKERS);
    }

    private void processCancelCommand(Long chatId) {
        userStates.invalidate(chatId);
        userEntities.invalidate(chatId);
        sendMessage(chatId, "Операция отменена.");
    }

    private void processRefreshCommand(Long chatId) {
        Optional<UserEntity> optionalUser = userService.getUserByChatId(chatId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            sendMessage(chatId, "Обновление данных, пожалуйста, подождите...");
            CompletableFuture.runAsync(() -> {
                try {
                    userProcessingService.processUserAsync(user.getId());
                    sendMessage(chatId, "Данные успешно обновлены.");
                } catch (Exception e) {
                    log.error("Ошибка при обновлении данных для пользователя {}: {}", user.getTelegramNickname(), e.getMessage());
                    sendMessage(chatId, "Произошла ошибка при обновлении данных.");
                }
            }, executorService);
        } else {
            sendNotRegisteredMessage(chatId);
        }
    }

    private void processAddFavouritesCommand(Long chatId) {
        sendMessage(chatId, MessageConstants.ADD_TICKERS_MESSAGE);
        userStates.put(chatId, UserState.WAITING_FOR_TICKERS_TO_ADD);
    }

    private void processRemoveFavouritesCommand(Long chatId) {
        sendMessage(chatId, MessageConstants.REMOVE_TICKERS_MESSAGE);
        userStates.put(chatId, UserState.WAITING_FOR_TICKERS_TO_REMOVE);
    }

    private void handleUserState(Long chatId, String messageText, Update update) {
        UserState state = userStates.getIfPresent(chatId);
        if (state != null) {
            switch (state) {
                case TOKEN -> processTokenInput(chatId, messageText, update);
                case PASSWORD -> processPasswordInput(chatId, messageText);
                case WAITING_FOR_NEW_TOKEN -> processNewTokenInput(chatId, messageText, update);
                case WAITING_FOR_PULSE_TICKERS -> processPulseTickers(chatId, messageText);
                case WAITING_FOR_CHOOSE_TYPE, WAITING_FOR_NEXT_COMMAND -> processChooseType(chatId, messageText);
                case WAITING_FOR_NEWS_TICKERS -> processNewsTickers(chatId, messageText);
                case WAITING_FOR_TICKERS_TO_ADD -> processTickersToAdd(chatId, messageText, update);
                case WAITING_FOR_TICKERS_TO_REMOVE -> processTickersToRemove(chatId, messageText, update);
                default -> promptForCommand(chatId);
            }
        } else {
            promptForCommand(chatId);
        }
    }

    private void processPulseTickers(Long chatId, String messageText) {
        if (messageText.matches(MessageConstants.TICKERS_LIST_PATTERN)) {
            List<String> tickers = Arrays.asList(messageText.split(","));
            stocksForInfoCache.put(chatId, tickers);
            sendMessage(chatId, MessageConstants.CHOOSE_TYPE_MESSAGE);
            userStates.put(chatId, UserState.WAITING_FOR_CHOOSE_TYPE);
        } else {
            sendMessage(chatId, MessageConstants.INVALID_TICKERS_MESSAGE);
        }
    }

    private void processNewsTickers(Long chatId, String messageText) {
        if (messageText.matches(MessageConstants.TICKERS_LIST_PATTERN)) {
            List<String> tickers = Arrays.asList(messageText.split(","));
            AnalyticsRequestDto requestDto = new AnalyticsRequestDto(AnalyticsInfoType.NEWS, chatId, tickers);
            requestService.sendRequest(requestDto);
            sendMessage(chatId, MessageConstants.SENT_REQUEST_MESSAGE);
            userStates.put(chatId, UserState.WAITING_FOR_NEWS_RESPONSE);
        } else {
            sendMessage(chatId, MessageConstants.INVALID_TICKERS_MESSAGE);
        }
    }

    private void processTickersToAdd(Long chatId, String messageText, Update update) {
        validateTickersMessage(chatId, messageText);
        List<String> tickers = Arrays.asList(messageText.split(","));
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());
        try {
            log.info("trying to add tickers {} to {}", telegramNickname, tickers);
            userService.addUserFavouriteStocks(telegramNickname, tickers);
        } catch(Exception e) {
            log.error("Error adding user favourite stocks {}", e.getMessage());
            sendMessage(chatId, MessageConstants.FAVORITES_OPERATION_FAIL);
            return;
        }
        sendMessage(chatId, MessageConstants.FAVORITES_OPERATION_SUCCESS);
        favouriteStockCache.invalidate(chatId);
    }

    private void processTickersToRemove(Long chatId, String messageText, Update update) {
        validateTickersMessage(chatId, messageText);
        List<String> tickers = Arrays.asList(messageText.split(","));
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());
        try {
            userService.deleteUserFavouriteStocks(telegramNickname, tickers);
        } catch(Exception e) {
            log.error("Error deleting user favourite stocks {}", e.getMessage());
            sendMessage(chatId, MessageConstants.FAVORITES_OPERATION_FAIL);
            return;
        }
        sendMessage(chatId, MessageConstants.FAVORITES_OPERATION_SUCCESS);
        favouriteStockCache.invalidate(chatId);
    }

    private void processChooseType(Long chatId, String messageText) {
        TinkoffPulseCommand command = TinkoffPulseCommand.fromValue(messageText);
        if (command == null) {
            sendMessage(chatId, "Неизвестная команда. Попробуйте еще раз");
            return;
        }

        List<String> tickers = stocksForInfoCache.getIfPresent(chatId);
        if (tickers == null) {
            sendMessage(chatId, "Тикеры компаний отсутствуют. Попробуйте еще раз написать тикеры");
            userStates.put(chatId, UserState.WAITING_FOR_PULSE_TICKERS);
        }

        AnalyticsRequestDto requestDto = new AnalyticsRequestDto(null, chatId, tickers);
        switch (command) {
            case NEWS:
                requestDto.setType(AnalyticsInfoType.PULSE_NEWS);
                break;
            case IDEAS:
                requestDto.setType(AnalyticsInfoType.IDEAS);
                break;
            case BRAND_INFO:
                requestDto.setType(AnalyticsInfoType.BRAND_INFO);
                break;
        }

        requestService.sendRequest(requestDto);
        sendMessage(chatId, MessageConstants.SENT_REQUEST_MESSAGE);
        userStates.put(chatId, UserState.WAITING_FOR_PULSE_RESPONSE);
    }

    private void processTokenInput(Long chatId, String token, Update update) {
        String validatedToken = TokenUtils.validateToken(token) ? token : "";
        if (validatedToken.isEmpty()) {
            sendMessage(chatId, MessageConstants.INVALID_TOKEN_MESSAGE);
        }
        UserEntity user = createUserEntityWithToken(validatedToken, update);
        userEntities.put(chatId, user);

        sendMessage(chatId, "Ваш Tinkoff токен сохранен. Теперь введите пароль.");
        userStates.put(chatId, UserState.PASSWORD);
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
        String validatedToken = TokenUtils.validateToken(newToken) ? newToken : "";

        if (validatedToken.isEmpty()) {
            sendMessage(chatId, MessageConstants.INVALID_TOKEN_MESSAGE);
        }

        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> {
                    sendMessage(chatId, "Обновление токена, пожалуйста, подождите...");
                    CompletableFuture.runAsync(() -> {
                        try {
                            userService.updateUserToken(user.getTelegramNickname(),
                                                        EncryptionService.encrypt(validatedToken));
                            sendMessage(chatId, "Ваш Tinkoff токен обновлен.");
                            clearUserSession(chatId);
                        } catch (Exception e) {
                            log.error("Ошибка при обновлении токена для пользователя {}: {}", user.getTelegramNickname(), e.getMessage());
                            sendMessage(chatId, "Произошла ошибка при обновлении токена.");
                        }
                    }, executorService);
                },
                () -> sendMessage(chatId, "Пользователь не найден. Пожалуйста, зарегистрируйтесь сначала.")
        );
    }

    private void promptForTinkoffToken(Long chatId) {
        String messageText = "Пожалуйста, предоставьте ваш <a href=\"https://www.tbank.ru/invest/settings/api/\">Tinkoff токен</a>.";
        sendMessage(chatId, messageText, "HTML", true);
        userStates.put(chatId, UserState.TOKEN);
    }

    private void promptForCommand(Long chatId) {
        sendMessage(chatId, MessageConstants.UNKNOWN_COMMAND_MESSAGE);
    }

    private void processHelpCommand(Long chatId) {
        String helpMessage = MessageConstants.HELP_MESSAGE;
        sendMessage(chatId, helpMessage);
    }

    private void sendNotRegisteredMessage(Long chatId) {
        log.debug("Attempt to access restricted feature by unregistered user with chatId: {}", chatId);
        sendMessage(chatId, "Вы не зарегистрированы. Пожалуйста, начните с команды /start.");
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
            sendNotRegisteredMessage(chatId);
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
        return MessageConstants.USER_INFO_MESSAGE.formatted(
                userInfo.getExternalId(),
                getSafeValue(userInfo.getTelegramNickname())
        );
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

    private void validateTickersMessage(Long chatId, String text) {
        if(!text.matches(MessageConstants.TICKERS_LIST_PATTERN)) {
            sendMessage(chatId, MessageConstants.INVALID_TICKERS_MESSAGE);
            log.error("invalid tickers message: {} in chat: {}", text, chatId);
            throw new RuntimeException("Invalid tickers messsage: " + text + " in chat: " + chatId);
        }
    }
}
