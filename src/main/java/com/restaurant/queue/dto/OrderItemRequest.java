package com.restaurant.queue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "Dish ID is required")
        Long dishId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        String notes
) {}
