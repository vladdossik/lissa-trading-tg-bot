package lissa.trading.tg.bot.analytics.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lissa.trading.tg.bot.analytics.AnalyticsInfoType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AnalyticsNewsPulseResponseDto.class, name = "PULSE_NEWS"),
        @JsonSubTypes.Type(value = AnalyticsIdeasPulseResponseDto.class, name = "IDEAS"),
        @JsonSubTypes.Type(value = AnalyticsBrandInfoResponseDto.class, name = "BRAND_INFO")
})
public class AnalyticsPulseResponseDto {
    private AnalyticsInfoType type;
    private Long chatId;
}
