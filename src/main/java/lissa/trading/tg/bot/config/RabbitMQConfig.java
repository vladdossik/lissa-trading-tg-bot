package lissa.trading.tg.bot.config;

import lissa.trading.tg.bot.analytics.dto.AnalyticsBrandInfoResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsIdeasPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsPulseResponseDto;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "notificationQueue";

    @Value("${integration.rabbit.outbound.analytics.queue}")
    private String requestQueue;

    @Value("${integration.rabbit.inbound.analytics.pulse.queue}")
    private String pulseResponseQueue;

    @Value("${integration.rabbit.inbound.analytics.news.queue}")
    private String newsResponseQueue;

    @Value("${integration.rabbit.exchange}")
    private String analyticsExchange;

    @Value("${integration.rabbit.outbound.analytics.routing-key.request.key}")
    private String requestRoutingKey;

    @Value("${integration.rabbit.outbound.analytics.routing-key.response.pulse.key}")
    private String responsePulseRoutingKey;

    @Value("${integration.rabbit.outbound.analytics.routing-key.response.news.key}")
    private String responseNewsRoutingKey;

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(analyticsExchange);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean("requestQueue")
    public Queue requestQueue() {
        return new Queue(requestQueue, true);
    }

    @Bean("pulseResponseQueue")
    public Queue pulseResponseQueue() {
        return new Queue(pulseResponseQueue, true);
    }

    @Bean("newsResponseQueue")
    public Queue newsResponseQueue() {
        return new Queue(newsResponseQueue, true);
    }

    @Bean
    public Binding requestBinding(@Qualifier("requestQueue") Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with(requestRoutingKey);
    }

    @Bean
    public Binding pulseResponseBinding(@Qualifier("pulseResponseQueue") Queue responseQueue,
                                        TopicExchange topicExchange) {
        return BindingBuilder.bind(responseQueue).to(topicExchange).with(responsePulseRoutingKey);
    }

    @Bean
    public Binding newResponseBinding(@Qualifier("newsResponseQueue") Queue responseQueue,
                                      TopicExchange topicExchange) {
        return BindingBuilder.bind(responseQueue).to(topicExchange).with(responseNewsRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }
}