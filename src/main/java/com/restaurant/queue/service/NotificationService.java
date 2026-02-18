package com.restaurant.queue.service;

import com.restaurant.queue.dto.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public <T> void notifyQueue(T payload) {
        messagingTemplate.convertAndSend("/topic/queue",
                WebSocketMessage.of("QUEUE_UPDATE", payload));
    }

    public <T> void notifyOrder(Long orderId, T payload) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId,
                WebSocketMessage.of("ORDER_UPDATE", payload));
    }

    public <T> void notifyKitchen(T payload) {
        messagingTemplate.convertAndSend("/topic/kitchen",
                WebSocketMessage.of("KITCHEN_UPDATE", payload));
    }
}
