package lissa.trading.tg.bot.analytics;

import lissa.trading.tg.bot.analytics.dto.AnalyticsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${integration.rabbit.analytics-service.request-queue}")
    private String requestQueue;

    public void sendRequest(AnalyticsRequestDto request) {
        log.info("Sending message {}", request);
        rabbitTemplate.convertAndSend(requestQueue, request);
        log.info("Successfully sent request to analytics service");
    }
}
