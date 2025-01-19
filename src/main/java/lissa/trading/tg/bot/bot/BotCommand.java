package lissa.trading.tg.bot.bot;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start"),
    TOKEN("/token"),
    INFO("/info"),
    FAVOURITES("/favourites"),
    PULSE("/pulse"),
    NEWS("/news"),
    CANCEL("/cancel"),
    REFRESH("/refresh"),
    HELP("/help"),
    PULSE("/pulse"),
    NEWS("/news"),
    UNKNOWN("");

    private static final Map<String, BotCommand> COMMAND_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(BotCommand::getValue, command -> command));

    private final String value;

    BotCommand(String value) {
        this.value = value;
    }

    public static BotCommand fromValue(String value) {
        return COMMAND_MAP.getOrDefault(value, UNKNOWN);
    }

    public String getValue() {
        return value;
    }
}
