package com.restaurant.queue.controller;

import com.restaurant.queue.dto.OrderResponse;
import com.restaurant.queue.dto.QueueResponse;
import com.restaurant.queue.service.QueueService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping
    public QueueResponse getQueue() {
        return queueService.getQueueStatus();
    }

    @PatchMapping("/next")
    public OrderResponse processNext() {
        return queueService.processNextOrder();
    }

    @PatchMapping("/{id}/complete")
    public OrderResponse completeOrder(@PathVariable Long id) {
        return queueService.completeOrder(id);
    }

    @PatchMapping("/{id}/deliver")
    public OrderResponse deliverOrder(@PathVariable Long id) {
        return queueService.deliverOrder(id);
    }
}
