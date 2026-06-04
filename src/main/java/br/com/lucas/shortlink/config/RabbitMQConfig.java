package br.com.lucas.shortlink.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_ANALYTICS = "analytics.queue";
    public static final String EXCHANGE_ANALYTICS = "analytics.exchange";
    public static final String ROUTING_KEY_ANALYTICS = "analytics.routing.key";

    @Bean
    public Queue analyticsQueue() {
        return new Queue(QUEUE_ANALYTICS, true); // true = fila durável (não se perde se o RabbitMQ reiniciar)
    }

    @Bean
    public DirectExchange analyticsExchange() {
        return new DirectExchange(EXCHANGE_ANALYTICS);
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, DirectExchange analyticsExchange) {
        return BindingBuilder.bind(analyticsQueue).to(analyticsExchange).with(ROUTING_KEY_ANALYTICS);
    }

    // Garante que os objetos trafeguem como JSON na fila, e não como bytes obscuros
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}