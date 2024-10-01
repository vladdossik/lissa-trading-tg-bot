package lissa.trading.tg.bot.bot;

import lombok.Getter;

@Getter
public enum BotCommand {
    START("/start"),
    TOKEN("/token"),
    INFO("/info"),
    UNKNOWN("");

    private final String value;

    BotCommand(String value) {
        this.value = value;
    }

    public static BotCommand fromValue(String value) {
        for (BotCommand command : values()) {
            if (command.value.equals(value)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}