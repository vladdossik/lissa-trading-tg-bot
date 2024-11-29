package lissa.trading.tg.bot.analytics.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lissa.trading.tg.bot.analytics.AnalyticsInfoType;
import lissa.trading.tg.bot.analytics.CustomResponseDtoDeserializer;
import lissa.trading.tg.bot.dto.tinkoffPulse.PulseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsPulseResponseDto {
    private AnalyticsInfoType type;
    private Long chatId;
    @JsonDeserialize(using = CustomResponseDtoDeserializer.class)
    private List<PulseResponseDto> data;
}
