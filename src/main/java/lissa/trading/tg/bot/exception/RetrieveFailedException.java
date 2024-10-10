package lissa.trading.tg.bot.exception;

public class RetrieveFailedException extends RuntimeException {
    public RetrieveFailedException(String message) {
        super(message);
    }

    public RetrieveFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
