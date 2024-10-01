package lissa.trading.tg.bot.exception;

public class ErrorGettingApplicationContextException extends RuntimeException {
    public ErrorGettingApplicationContextException(String message) {
        super(message);
    }

    public ErrorGettingApplicationContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
