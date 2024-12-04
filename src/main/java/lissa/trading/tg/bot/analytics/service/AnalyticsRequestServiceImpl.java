package lissa.trading.tg.bot.analytics.service;

import lissa.trading.tg.bot.analytics.AnalyticsSender;
import lissa.trading.tg.bot.analytics.dto.AnalyticsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsRequestServiceImpl implements AnalyticsRequestService {

    private final AnalyticsSender analyticsSender;

    @Override
    public void sendRequest(AnalyticsRequestDto requestDto) {
        analyticsSender.sendRequest(requestDto);
    }
}
