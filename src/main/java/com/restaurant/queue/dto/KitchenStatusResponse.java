package com.restaurant.queue.dto;

public record KitchenStatusResponse(
        int totalStations,
        int activeStations,
        int availableStations,
        double loadPercentage,
        int pendingOrders,
        int preparingOrders,
        double averageWaitMinutes
) {}
