package lissa.trading.tg.bot.bot;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final UserService userService;
    private final Map<Long, UserState> userStates = new HashMap<>();  // Состояния пользователей
    private final Map<Long, UserEntity> userEntities = new HashMap<>(); // Временное хранилище для сущностей пользователей

    public TelegramBot(String botName, String botToken, UserService userService) {
        super(botToken);
        this.botName = botName;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
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
            case START -> promptForToken(update);
            case TOKEN -> requestNewToken(chatId);
            case INFO -> sendInfo(chatId, update);
        }
    }

    private void handleUserState(Long chatId, String messageText, Update update) {
        UserState state = userStates.getOrDefault(chatId, UserState.TOKEN);
        switch (state) {
            case TOKEN -> processToken(chatId, messageText, update);
            case PASSWORD -> processPassword(chatId, messageText);
            case WAITING_FOR_NEW_TOKEN -> processNewToken(chatId, messageText, update);
        }
    }

    // Метод запроса токена
    private void promptForToken(Update update) {
        Long chatId = update.getMessage().getChatId();
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        if (userService.userExistsByTelegramNickname(telegramNickname)) {
            UserInfoDto user = userService.getUserByTelegramNickname(telegramNickname);
            respondBasedOnTokenPresence(chatId, user);
        } else {
            initiateTokenRegistration(chatId);
        }
    }

    // Обработка токена пользователя
    private void processToken(Long chatId, String token, Update update) {
        if (isValidToken(token)) {
            UserEntity user = new UserEntity();
            user.setTinkoffToken(EncryptionService.encrypt(token));
            registerUserDetails(user, update);

            userEntities.put(chatId, user);

            sendMessage(chatId, "Your Tinkoff token has been saved. Now, please provide a password.");
            userStates.put(chatId, UserState.PASSWORD);
        } else {
            sendMessage(chatId, "Invalid Tinkoff token. It must start with 't.' and be 88 characters long.");
        }
    }

    // Обработка пароля пользователя
    private void processPassword(Long chatId, String password) {
        if (isValidPassword(password)) {
            UserEntity user = getTemporaryUser(chatId);

            user.setPassword(EncryptionService.encrypt(password));

            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setFirstName(user.getFirstName());
            signupRequest.setLastName(user.getLastName());
            signupRequest.setTelegramNickname(user.getTelegramNickname());
            signupRequest.setTinkoffToken(user.getTinkoffToken());
            signupRequest.setPassword(password);
            Set<String> roles = new HashSet<>();
            roles.add("user");
            signupRequest.setRole(roles);

            userService.registerUser(signupRequest);
            finalizeRegistration(chatId, "Registration complete.");
        } else {
            sendMessage(chatId, "Password must be at least 3 characters long. Please try again.");
        }
    }


    // Запрос нового токена
    private void requestNewToken(Long chatId) {
        sendMessage(chatId, "Please provide your new Tinkoff token.");
        userStates.put(chatId, UserState.WAITING_FOR_NEW_TOKEN);
    }

    // Обработка нового токена
    private void processNewToken(Long chatId, String newToken, Update update) {
        if (isValidToken(newToken)) {
            String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

            UserInfoDto user = userService.getUserByTelegramNickname(telegramNickname);
            userService.updateUserToken(user.getTelegramNickname(), EncryptionService.encrypt(newToken));

            sendMessage(chatId, "Your Tinkoff token has been updated.");
        } else {
            sendMessage(chatId, "Invalid Tinkoff token. Please provide a valid token.");
        }
    }

    private void sendInfo(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        if (userService.userExistsByTelegramNickname(telegramNickname)) {
            UserInfoDto userInfo = userService.getUserByTelegramNickname(telegramNickname);

            sendMessage(chatId, formatUserInfo(userInfo));
        } else {
            sendMessage(chatId, "You are not registered in the system. Please start by providing your Tinkoff token.");
        }
    }


    // Вспомогательные методы
    private UserEntity getTemporaryUser(Long chatId) {
        UserEntity user = userEntities.get(chatId);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }
        return user;
    }

    private void finalizeRegistration(Long chatId, String message) {
        sendMessage(chatId, message);
        userStates.remove(chatId);
        userEntities.remove(chatId);
    }

    private void initiateTokenRegistration(Long chatId) {
        sendMessage(chatId, "Please provide your Tinkoff token to continue.");
        userStates.put(chatId, UserState.TOKEN);
    }

    private void respondBasedOnTokenPresence(Long chatId, UserInfoDto user) {
        if (user.getTinkoffToken() != null) {
            sendMessage(chatId, "You are already authorized with a Tinkoff token.");
        } else {
            sendMessage(chatId, "No Tinkoff token found. Please provide your Tinkoff token.");
        }
    }

    private void registerUserDetails(UserEntity user, Update update) {
        user.setTelegramNickname(getSafeValue(update.getMessage().getFrom().getUserName()));
        user.setFirstName(getSafeValue(update.getMessage().getFrom().getFirstName()));
        user.setLastName(getSafeValue(update.getMessage().getFrom().getLastName()));
    }

    private String formatUserInfo(UserInfoDto userInfo) {
        return """
               {
                   "externalId": "%s",
                   "firstName": "%s",
                   "lastName": "%s",
                   "telegramNickname": "%s",
                   "decryptedTinkoffToken": "%s",
                   "roles": %s
               }
               """.formatted(
                userInfo.getExternalId(),
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getTelegramNickname(),
                EncryptionService.decrypt(userInfo.getTinkoffToken()),
                userInfo.getRoles().toString()
        );
    }

    // Валидация
    private boolean isValidToken(String token) {
        return token.startsWith("t.") && token.length() == 88;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 3;
    }

    // Отправка сообщений
    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: ", e);
        }
    }

    // Утилиты
    private String getSafeValue(String value) {
        return value != null ? value : "Not provided";
    }
}