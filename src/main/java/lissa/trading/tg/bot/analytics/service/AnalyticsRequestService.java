package lissa.trading.tg.bot.analytics.service;

import lissa.trading.tg.bot.analytics.dto.AnalyticsRequestDto;

public interface AnalyticsRequestService {
    void sendRequest(AnalyticsRequestDto requestDto);
}
