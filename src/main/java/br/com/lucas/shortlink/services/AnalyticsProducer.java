package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.config.RabbitMQConfig;
import br.com.lucas.shortlink.dtos.request.ClickEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsProducer {

    private final RabbitTemplate rabbitTemplate;

    public AnalyticsProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendClickEvent(ClickEventDTO event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ANALYTICS,
                RabbitMQConfig.ROUTING_KEY_ANALYTICS,
                event
        );
    }
}