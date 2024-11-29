package lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo;

import lissa.trading.tg.bot.dto.tinkoffPulse.PulseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandInfoPulseResponseDto extends PulseResponseDto {
    private BrandInfoDto brandInfo;
}

