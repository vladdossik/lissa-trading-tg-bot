package lissa.trading.tg.bot.bot;

public enum UserState {
    TOKEN,     // Ожидание ввода токена
    PASSWORD,   // Ожидание ввода пароля
    WAITING_FOR_NEW_TOKEN, // Ожидание нового токена
    WAITING_FOR_PULSE_TICKERS,
    WAITING_FOR_NEWS_TICKERS,
    WAITING_FOR_CHOOSE_TYPE,
    WAITING_FOR_NEWS_RESPONSE,
    WAITING_FOR_PULSE_RESPONSE,
    WAITING_FOR_NEXT_COMMAND,
    WAITING_FOR_TICKERS_TO_ADD,
    WAITING_FOR_TICKERS_TO_REMOVE
}