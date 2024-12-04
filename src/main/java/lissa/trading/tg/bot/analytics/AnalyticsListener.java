package lissa.trading.tg.bot.analytics;

import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsPulseResponseDto;
import lissa.trading.tg.bot.analytics.service.AnalyticsResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsListener {

    private final AnalyticsResponseService responseService;

    @RabbitListener(queues = "${integration.rabbit.queues.inbound.analytics.pulse}")
    private void handlePulseResponse(AnalyticsPulseResponseDto responseDto) {
        if (responseDto != null) {
            log.info("Successfully get pulse response");
            responseService.processPulseResponse(responseDto);
        }
    }

    @RabbitListener(queues = "${integration.rabbit.queues.inbound.analytics.news}")
    private void handleNewsResponse(AnalyticsNewsResponseDto newsResponseDto) {
        if (newsResponseDto != null) {
            log.info("Successfully get news response");
            responseService.processNewsResponse(newsResponseDto);
        }
    }
}
