package lissa.trading.tg.bot.bot;

public class ChatIdContext {

    private ChatIdContext() {
    }

    private static final ThreadLocal<Long> chatIdHolder = new ThreadLocal<>();

    public static void setChatId(Long chatId) {
        if (chatId != null) {
            chatIdHolder.set(chatId);
        }
    }

    public static Long getChatId() {
        return chatIdHolder.get();
    }

    public static void clear() {
        chatIdHolder.remove();
    }

    public static void runWithChatId(Long chatId, Runnable task) {
        try {
            setChatId(chatId);
            task.run();
        } finally {
            clear();
        }
    }
}
