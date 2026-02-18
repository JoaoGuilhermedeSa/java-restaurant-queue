package com.restaurant.queue.dto;

import java.time.LocalDateTime;

public record EstimateDetails(
        Long orderId,
        double baseTimeMinutes,
        double complexityAdjustedTimeMinutes,
        int ordersAhead,
        double parallelismFactor,
        double waitMinutes,
        double prepMinutes,
        double totalMinutes,
        LocalDateTime estimatedCompletionTime
) {}
