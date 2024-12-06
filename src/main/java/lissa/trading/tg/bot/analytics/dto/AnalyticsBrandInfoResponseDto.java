package lissa.trading.tg.bot.analytics.dto;

import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoPulseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsBrandInfoResponseDto extends AnalyticsPulseResponseDto {
    private List<BrandInfoPulseResponseDto> data;
}