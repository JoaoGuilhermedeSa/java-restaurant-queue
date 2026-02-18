package com.restaurant.queue.dto;

import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DishRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Category is required")
        DishCategory category,

        @Min(value = 1, message = "Preparation time must be at least 1 minute")
        int basePreparationTimeMinutes,

        @NotNull(message = "Complexity is required")
        DishComplexity complexity
) {}
