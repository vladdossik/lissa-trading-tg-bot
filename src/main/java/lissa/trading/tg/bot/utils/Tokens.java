package lissa.trading.tg.bot.utils;

import lissa.trading.lissa.auth.lib.security.EncryptionService;

public class Tokens {
    public static boolean validateToken(String token) {
        return token.startsWith("t.") && token.length() == 88;
    }

    public static String encryptToken(String token) {
        if (validateToken(token) || !token.isEmpty()) {
            return EncryptionService.encrypt(token);
        }
        return token;
    }
}
