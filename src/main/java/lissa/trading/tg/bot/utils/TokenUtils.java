package lissa.trading.tg.bot.utils;

import lissa.trading.lissa.auth.lib.security.EncryptionService;

public class TokenUtils {
    private final static String TINKOFF_TOKEN_STARTS_WITH = "t.";
    private final static int TINKOFF_TOKEN_LENGTH = 88;

    public static boolean validateToken(String token) {
        return token.startsWith(TINKOFF_TOKEN_STARTS_WITH)
                && token.length() == TINKOFF_TOKEN_LENGTH;
    }

    public static String encryptToken(String token) {
        if (validateToken(token) || !token.isEmpty()) {
            return EncryptionService.encrypt(token);
        }
        return token;
    }
}
