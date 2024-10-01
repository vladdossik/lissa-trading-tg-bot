package lissa.trading.tg.bot.bot;

public class ChatIdContext {
    private static final ThreadLocal<Long> chatIdHolder = new ThreadLocal<>();

    public static void setChatId(Long chatId) {
        chatIdHolder.set(chatId);
    }

    public static Long getChatId() {
        return chatIdHolder.get();
    }

    public static void clear() {
        chatIdHolder.remove();
    }
}