package lissa.trading.tg.bot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${integration.rabbit.user-service.queues.favourite-stocks-queue.name}")
    private String userServiceFavoriteStocksQueue;

    @Value("${integration.rabbit.user-service.queues.user-update-queue.name}")
    private String userServiceUpdateQueue;

    @Value("${integration.rabbit.tg-bot.queues.favorite-stocks-queue.name}")
    private String tgBotFavoriteStocksQueue;

    @Value("${integration.rabbit.tg-bot.queues.user-update-queue.name}")
    private String tgBotUserUpdateQueue;

    @Value("${integration.rabbit.outbound.analytics.exchange}")
    private String analyticsExchange;

    @Value("${integration.rabbit.exchanges.user-notifications}")
    private String exchange;

    @Value("${integration.rabbit.outbound.analytics.routing-key}")
    private String requestRoutingKey;

    @Value("${integration.rabbit.inbound.analytics.pulse.routing-key}")
    private String responsePulseRoutingKey;

    @Value("${integration.rabbit.inbound.analytics.news.routing-key}")
    private String responseNewsRoutingKey;

    @Value("${integration.rabbit.user-service.queues.favourite-stocks-queue.routing-key}")
    private String userServiceFavouriteStocksQueueRoutingKey;

    @Value("${integration.rabbit.user-service.queues.user-update-queue.routing-key}")
    private String userServiceUpdateQueueRoutingKey;

    @Value("${integration.rabbit.tg-bot.queues.favorite-stocks-queue.routing-key}")
    private String tgBotFavouriteStocksQueueRoutingKey;

    @Value("${integration.rabbit.tg-bot.queues.user-update-queue.routing-key}")
    private String tgBotUserUpdateQueueRoutingKey;

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
    public Queue userServiceFavoriteStocksQueue() {
        return new Queue(userServiceFavoriteStocksQueue, true);
    }

    @Bean
    public Queue userServiceUserUpdateQueue() {
        return new Queue(userServiceUpdateQueue, true);
    }

    @Bean
    public Queue tgBotFavoriteStocksQueue() {return new Queue(tgBotFavoriteStocksQueue, true);}

    @Bean
    public Queue tgBotUserUpdateQueue() {return new Queue(tgBotUserUpdateQueue, true);}

    @Bean(name = "analyticsExchange")
    public TopicExchange analyticsTopicExchange() {
        return new TopicExchange(analyticsExchange);
    }

    @Bean(name = "userExchange")
    public TopicExchange userExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding userServiceFavoriteStocksBinding(Queue userServiceFavoriteStocksQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userServiceFavoriteStocksQueue)
                .to(userExchange)
                .with(userServiceFavouriteStocksQueueRoutingKey);
    }

    @Bean
    public Binding userServiceUserUpdateBinding(Queue userServiceUserUpdateQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userServiceUserUpdateQueue)
                .to(userExchange)
                .with(userServiceUpdateQueueRoutingKey);
    }

    @Bean
    public Binding tgBotFavoriteStocksBinding(Queue tgBotFavoriteStocksQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(tgBotFavoriteStocksQueue)
                .to(userExchange)
                .with(tgBotFavouriteStocksQueueRoutingKey);
    }

    @Bean
    public Binding tgBotUserUpdateBinding(Queue tgBotUserUpdateQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(tgBotUserUpdateQueue)
                .to(userExchange)
                .with(tgBotUserUpdateQueueRoutingKey);
    }

    @Bean
    public Binding requestBinding(Queue requestQueue,
                                  @Qualifier("analyticsExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with(requestRoutingKey);
    }

    @Bean
    public Binding pulseResponseBinding(Queue pulseResponseQueue,
                                        @Qualifier("analyticsExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(pulseResponseQueue).to(topicExchange).with(responsePulseRoutingKey);
    }

    @Bean
    public Binding newResponseBinding(Queue newsResponseQueue,
                                      @Qualifier("analyticsExchange") TopicExchange topicExchange) {
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