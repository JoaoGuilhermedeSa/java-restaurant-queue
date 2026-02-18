package com.restaurant.queue.service;

import com.restaurant.queue.config.KitchenConfig;
import com.restaurant.queue.dto.KitchenStatusResponse;
import com.restaurant.queue.entity.Order;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KitchenStatusService {

    private final OrderRepository orderRepository;
    private final EstimationService estimationService;
    private final KitchenConfig kitchenConfig;

    public KitchenStatusService(OrderRepository orderRepository, EstimationService estimationService,
                                KitchenConfig kitchenConfig) {
        this.orderRepository = orderRepository;
        this.estimationService = estimationService;
        this.kitchenConfig = kitchenConfig;
    }

    public KitchenStatusResponse getKitchenStatus() {
        int totalStations = kitchenConfig.stations();
        int preparingCount = (int) orderRepository.countByStatus(OrderStatus.PREPARING);
        int pendingCount = (int) orderRepository.countByStatus(OrderStatus.PENDING);
        int activeStations = Math.min(preparingCount, totalStations);
        int availableStations = totalStations - activeStations;
        double loadPercentage = totalStations > 0 ? (activeStations * 100.0) / totalStations : 0;

        double averageWaitMinutes = calculateAverageWaitMinutes();

        return new KitchenStatusResponse(
                totalStations,
                activeStations,
                availableStations,
                loadPercentage,
                pendingCount,
                preparingCount,
                averageWaitMinutes
        );
    }

    private double calculateAverageWaitMinutes() {
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        if (pendingOrders.isEmpty()) {
            return 0;
        }
        double totalWait = pendingOrders.stream()
                .mapToDouble(order -> {
                    var estimate = estimationService.calculateEstimateDetails(order);
                    return estimate.totalMinutes();
                })
                .sum();
        return totalWait / pendingOrders.size();
    }
}
