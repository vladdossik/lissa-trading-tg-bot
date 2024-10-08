package lissa.trading.tg.bot.bot;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum BotCommand {
    START("/start"),
    TOKEN("/token"),
    INFO("/info"),
    UNKNOWN("");

    private static final Map<String, BotCommand> COMMAND_MAP = new HashMap<>();

    static {
        for (BotCommand command : values()) {
            COMMAND_MAP.put(command.value, command);
        }
    }

    private final String value;

    BotCommand(String value) {
        this.value = value;
    }

    public static BotCommand fromValue(String value) {
        return COMMAND_MAP.getOrDefault(value, UNKNOWN);
    }
}