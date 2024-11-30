package lissa.trading.tg.bot.analytics;

import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsPulseResponseDto;
import lissa.trading.tg.bot.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsListener {

    private final TelegramBot telegramBot;

    @RabbitListener(queues = "${integration.rabbit.inbound.analytics.pulse.queue}")
    private void handlePulseResponse(AnalyticsPulseResponseDto responseDto) {
        if (responseDto != null) {
            telegramBot.handlePulseResponse(responseDto);
        }
    }

    @RabbitListener(queues = "${integration.rabbit.inbound.analytics.news.queue}")
    private void handleNewsResponse(AnalyticsNewsResponseDto newsResponseDto) {
        if (newsResponseDto != null) {
            telegramBot.handleNewsResponse(newsResponseDto);
        }
    }
}
