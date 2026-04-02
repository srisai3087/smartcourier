package com.smartcourier.delivery.producer;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service  // 🔥 THIS IS REQUIRED
public class DeliveryEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public DeliveryEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEvent(String message) {
        rabbitTemplate.convertAndSend(
                "delivery.exchange",
                "delivery.created",
                message
        );
    }
}
