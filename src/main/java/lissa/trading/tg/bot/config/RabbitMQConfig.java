package lissa.trading.tg.bot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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

    @Value("${integration.rabbit.outbound.notification.queue}")
    public String notificationQueue;

    @Value("${integration.rabbit.outbound.analytics.queue}")
    private String requestQueue;

    @Value("${integration.rabbit.inbound.analytics.pulse.queue}")
    private String pulseResponseQueue;

    @Value("${integration.rabbit.inbound.analytics.news.queue}")
    private String newsResponseQueue;

    @Value("${integration.rabbit.outbound.analytics.exchange}")
    private String analyticsExchange;

    @Value("${integration.rabbit.outbound.analytics.routing-key}")
    private String requestRoutingKey;

    @Value("${integration.rabbit.inbound.analytics.pulse.routing-key}")
    private String responsePulseRoutingKey;

    @Value("${integration.rabbit.inbound.analytics.news.routing-key}")
    private String responseNewsRoutingKey;

    @Bean
    public TopicExchange analyticsTopicExchange() {
        return new TopicExchange(analyticsExchange);
    }

    @Value("${integration.rabbit.user-service.exchange.name}")
    private String exchange;

    @Value("${integration.rabbit.user-service.favourite-stocks-queue.name}")
    private String userFavoriteStocksQueue;

    @Value("${integration.rabbit.user-service.user-update-queue.name}")
    private String userUpdateQueue;

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
    public TopicExchange userExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue userFavoriteStocksQueue() {
        return new Queue(userFavoriteStocksQueue, true);
    }

    @Bean public Queue userUpdateQueue() {
        return new Queue(userUpdateQueue, true);
    }

    @Bean
    public Binding favoriteStocksBinding(Queue userFavoriteStocksQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userFavoriteStocksQueue)
                .to(userExchange)
                .with("*.favourite-stocks");
    }

    @Bean
    public Binding userUpdateBinding(Queue userUpdateQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userUpdateQueue)
                .to(userExchange)
                .with("*.user-update");
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