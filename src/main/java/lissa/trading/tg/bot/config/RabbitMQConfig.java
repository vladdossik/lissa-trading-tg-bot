package lissa.trading.tg.bot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${integration.rabbit.queues.outbound.notification}")
    public String notificationQueue;

    @Value("${integration.rabbit.queues.outbound.analytics.request}")
    private String requestQueue;

    @Value("${integration.rabbit.queues.inbound.analytics.pulse}")
    private String pulseResponseQueue;

    @Value("${integration.rabbit.queues.inbound.analytics.news}")
    private String newsResponseQueue;

    @Value("${integration.rabbit.exchanges.analytics}")
    private String analyticsExchange;

    @Value("${integration.rabbit.routing-keys.tg-bot.request}")
    private String requestRoutingKey;

    @Value("${integration.rabbit.routing-keys.tg-bot.response.pulse}")
    private String responsePulseRoutingKey;

    @Value("${integration.rabbit.routing-keys.tg-bot.response.news}")
    private String responseNewsRoutingKey;

    @Bean
    public TopicExchange analyticsTopicExchange() {
        return new TopicExchange(analyticsExchange);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    @Bean
    public Queue requestQueue() {
        return new Queue(requestQueue, true);
    }

    @Bean
    public Queue pulseResponseQueue() {
        return new Queue(pulseResponseQueue, true);
    }

    @Bean
    public Queue newsResponseQueue() {
        return new Queue(newsResponseQueue, true);
    }

    @Bean
    public Binding requestBinding(Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with(requestRoutingKey);
    }

    @Bean
    public Binding pulseResponseBinding(Queue pulseResponseQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(pulseResponseQueue).to(topicExchange).with(responsePulseRoutingKey);
    }

    @Bean
    public Binding newResponseBinding(Queue newsResponseQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(newsResponseQueue).to(topicExchange).with(responseNewsRoutingKey);
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