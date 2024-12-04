package lissa.trading.tg.bot.bot;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum TinkoffPulseCommand {
    NEWS("news"),
    IDEAS("ideas"),
    BRAND_INFO("brandInfo");

    private static final Map<String, TinkoffPulseCommand> COMMAND_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(TinkoffPulseCommand::getValue, command -> command));

    private final String value;

    TinkoffPulseCommand(String value) {
        this.value = value;
    }

    public static TinkoffPulseCommand fromValue(String value) {
        return COMMAND_MAP.get(value);
    }
}