package com.restaurant.queue.dto;

import java.util.List;

public record QueueResponse(
        List<OrderResponse> pendingOrders,
        List<OrderResponse> preparingOrders,
        int availableStations,
        int totalStations
) {}
