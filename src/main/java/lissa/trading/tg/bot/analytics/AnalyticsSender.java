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
public class AnalyticsSender {

    @Value("${integration.rabbit.exchange}")
    private String analyticsExchange;

    @Value("${integration.rabbit.outbound.analytics.routing-key.request.key}")
    private String requestRoutingKey;

    private final RabbitTemplate rabbitTemplate;


    public void sendRequest(AnalyticsRequestDto request) {
        log.info("Sending message {}", request);
        rabbitTemplate.convertAndSend(analyticsExchange, requestRoutingKey, request);
        log.info("Successfully sent request to queue with routingKey {}", requestRoutingKey);
    }
}