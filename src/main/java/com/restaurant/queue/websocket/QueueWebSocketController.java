package com.restaurant.queue.websocket;

import com.restaurant.queue.dto.QueueResponse;
import com.restaurant.queue.service.QueueService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class QueueWebSocketController {

    private final QueueService queueService;

    public QueueWebSocketController(QueueService queueService) {
        this.queueService = queueService;
    }

    @MessageMapping("/queue/subscribe")
    @SendTo("/topic/queue")
    public QueueResponse subscribeToQueue() {
        return queueService.getQueueStatus();
    }
}
