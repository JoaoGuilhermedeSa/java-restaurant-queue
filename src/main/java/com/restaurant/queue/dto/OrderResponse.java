package com.restaurant.queue.dto;

import com.restaurant.queue.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        List<OrderItemResponse> items,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime estimatedCompletionTime,
        LocalDateTime statusUpdatedAt
) {}
