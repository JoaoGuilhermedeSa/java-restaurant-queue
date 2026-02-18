package com.restaurant.queue.dto;

public record OrderItemResponse(
        Long id,
        DishResponse dish,
        int quantity,
        String notes
) {}
