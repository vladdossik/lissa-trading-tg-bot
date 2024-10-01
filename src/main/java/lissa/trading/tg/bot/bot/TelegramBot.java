package lissa.trading.tg.bot.bot;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final UserService userService;
    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, UserEntity> userEntities = new HashMap<>();

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
            case START -> promptForToken(chatId, update);
            case TOKEN -> requestNewToken(chatId);
            case INFO -> sendInfo(chatId, update);
            default -> sendMessage(chatId, "Unknown command.");
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

    private void promptForToken(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> respondBasedOnTokenPresence(chatId, user),
                () -> initiateTokenRegistration(chatId)
        );
    }

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

    private void processPassword(Long chatId, String password) {
        if (isValidPassword(password)) {
            UserEntity user = getTemporaryUser(chatId);

            if (user == null) {
                resetUserSession(chatId, "Session expired. Please start again.");
                return;
            }

            user.setPassword(EncryptionService.encrypt(password));

            SignupRequest signupRequest = buildSignupRequest(user, password);
            handleUserRegistration(chatId, signupRequest);
        } else {
            sendMessage(chatId, "Password must be at least 3 characters long. Please try again.");
        }
    }

    private SignupRequest buildSignupRequest(UserEntity user, String password) {
        return SignupRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .telegramNickname(user.getTelegramNickname())
                .tinkoffToken(user.getTinkoffToken())
                .password(password)
                .role(Set.of("user"))
                .build();
    }

    private void handleUserRegistration(Long chatId, SignupRequest signupRequest) {
        UserRegistrationResponse response = userService.registerUser(signupRequest);
        if (response.isSuccess()) {
            finalizeRegistration(chatId, "Registration complete.");
        } else {
            sendMessage(chatId, response.getMessage());
        }
    }

    private void requestNewToken(Long chatId) {
        sendMessage(chatId, "Please provide your new Tinkoff token.");
        userStates.put(chatId, UserState.WAITING_FOR_NEW_TOKEN);
    }

    private void processNewToken(Long chatId, String newToken, Update update) {
        if (isValidToken(newToken)) {
            String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

            userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                    user -> {
                        userService.updateUserToken(user.getTelegramNickname(), EncryptionService.encrypt(newToken));
                        sendMessage(chatId, "Your Tinkoff token has been updated.");
                    },
                    () -> sendMessage(chatId, "User not found. Please register first.")
            );
        } else {
            sendMessage(chatId, "Invalid Tinkoff token. Please provide a valid token.");
        }
    }

    private void sendInfo(Long chatId, Update update) {
        String telegramNickname = getSafeValue(update.getMessage().getFrom().getUserName());

        userService.getUserByTelegramNickname(telegramNickname).ifPresentOrElse(
                user -> sendMessage(chatId, formatUserInfo(user)),
                () -> sendMessage(chatId, "You are not registered in the system. Please start by providing your Tinkoff token.")
        );
    }

    private UserEntity getTemporaryUser(Long chatId) {
        return userEntities.get(chatId);
    }

    private void finalizeRegistration(Long chatId, String message) {
        sendMessage(chatId, message);
        userStates.remove(chatId);
        userEntities.remove(chatId);
    }

    private void resetUserSession(Long chatId, String message) {
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
                User Information:
                -----------------
                ID: %s
                Nickname: %s
                Token: %s
                """.formatted(
                userInfo.getExternalId(),
                getSafeValue(userInfo.getTelegramNickname()),
                EncryptionService.decrypt(userInfo.getTinkoffToken())
        );
    }


    private boolean isValidToken(String token) {
        return token.startsWith("t.") && token.length() == 88;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 3;
    }

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
            log.error("Error sending message: {}", e.getMessage());
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "Not provided";
    }
}