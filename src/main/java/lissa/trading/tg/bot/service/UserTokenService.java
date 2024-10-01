package lissa.trading.tg.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserTokenService {

    // Хранение токенов Tinkoff
    private final ConcurrentHashMap<Long, String> tinkoffTokens = new ConcurrentHashMap<>();

    // Хранение JWT токенов
    private final ConcurrentHashMap<Long, String> jwtTokens = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, String> refreshTokens = new ConcurrentHashMap<>();

    public void saveTinkoffToken(Long chatId, String tinkoffToken) {
        tinkoffTokens.put(chatId, tinkoffToken);
    }

    public String getTinkoffToken(Long chatId) {
        return tinkoffTokens.get(chatId);
    }

    public boolean hasTinkoffToken(Long chatId) {
        return tinkoffTokens.containsKey(chatId);
    }

    public void saveJwtToken(Long chatId, String jwtToken) {
        jwtTokens.put(chatId, jwtToken);
        log.info("JWT token {} saved for chatId: {}", jwtTokens, chatId);
    }

    public String getJwtToken(Long chatId) {
        return jwtTokens.get(chatId);
    }

    public boolean hasJwtToken(Long chatId) {
        return jwtTokens.containsKey(chatId);
    }

    public void saveRefreshToken(Long chatId, String jwtToken) {
        refreshTokens.put(chatId, jwtToken);
        log.info("Refresh token {} saved for chatId: {}", refreshTokens, chatId);
    }

    public String getRefreshToken(Long chatId) {
        return refreshTokens.get(chatId);
    }

    public boolean hasRefreshToken(Long chatId) {
        return refreshTokens.containsKey(chatId);
    }
}