package com.smartcourier.tracking.consumer;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeliveryEventConsumer {

    @RabbitListener(queues = "delivery.queue")
    public void receive(String message) {
        System.out.println("📩 Received: " + message);
    }
}