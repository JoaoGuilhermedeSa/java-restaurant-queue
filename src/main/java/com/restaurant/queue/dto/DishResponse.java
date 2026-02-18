package com.restaurant.queue.dto;

import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;

public record DishResponse(
        Long id,
        String name,
        String description,
        DishCategory category,
        int basePreparationTimeMinutes,
        DishComplexity complexity,
        boolean active
) {}
