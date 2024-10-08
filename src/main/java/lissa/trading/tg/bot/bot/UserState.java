package lissa.trading.tg.bot.bot;

public enum UserState {
    TOKEN,     // Ожидание ввода токена
    PASSWORD,   // Ожидание ввода пароля
    WAITING_FOR_NEW_TOKEN // Ожидание нового токена
}