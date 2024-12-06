package lissa.trading.tg.bot.analytics.service;

import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsPulseResponseDto;

public interface AnalyticsResponseService {
    void processPulseResponse(AnalyticsPulseResponseDto responseDto);

    void processNewsResponse(AnalyticsNewsResponseDto responseDto);
}
