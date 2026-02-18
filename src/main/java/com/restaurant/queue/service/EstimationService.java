package com.restaurant.queue.service;

import com.restaurant.queue.config.KitchenConfig;
import com.restaurant.queue.dto.EstimateDetails;
import com.restaurant.queue.entity.Order;
import com.restaurant.queue.entity.OrderItem;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EstimationService {

    private final KitchenConfig kitchenConfig;
    private final OrderRepository orderRepository;

    public EstimationService(KitchenConfig kitchenConfig, OrderRepository orderRepository) {
        this.kitchenConfig = kitchenConfig;
        this.orderRepository = orderRepository;
    }

    public EstimateDetails getEstimateDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return calculateEstimateDetails(order);
    }

    public EstimateDetails calculateEstimateDetails(Order order) {
        double baseTime = calculateBaseTime(order);
        double complexityAdjustedTime = calculateComplexityAdjustedTime(order);
        int itemCount = order.getItems().size();
        double parallelismFactor = Math.min(itemCount, kitchenConfig.stations());
        double prepMinutes = parallelismFactor > 0 ? complexityAdjustedTime / parallelismFactor : complexityAdjustedTime;

        List<Order> ordersAhead = getOrdersAhead(order);
        double waitMinutes = calculateWaitMinutes(ordersAhead);

        double totalMinutes = waitMinutes + prepMinutes;
        LocalDateTime estimatedCompletion = LocalDateTime.now().plusMinutes((long) Math.ceil(totalMinutes));

        return new EstimateDetails(
                order.getId(),
                baseTime,
                complexityAdjustedTime,
                ordersAhead.size(),
                parallelismFactor,
                waitMinutes,
                prepMinutes,
                totalMinutes,
                estimatedCompletion
        );
    }

    public void recalculateAllPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            EstimateDetails estimate = calculateEstimateDetails(order);
            order.setEstimatedCompletionTime(estimate.estimatedCompletionTime());
            orderRepository.save(order);
        }
    }

    double calculateBaseTime(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getDish().getBasePreparationTimeMinutes() * item.getQuantity())
                .sum();
    }

    double calculateComplexityAdjustedTime(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getDish().getBasePreparationTimeMinutes()
                        * item.getDish().getComplexity().getMultiplier()
                        * item.getQuantity())
                .sum();
    }

    double calculateOrderPrepTime(Order order) {
        double complexityAdjusted = calculateComplexityAdjustedTime(order);
        int itemCount = order.getItems().size();
        double parallelism = Math.min(itemCount, kitchenConfig.stations());
        return parallelism > 0 ? complexityAdjusted / parallelism : complexityAdjusted;
    }

    List<Order> getOrdersAhead(Order order) {
        List<Order> activeOrders = orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.PENDING, OrderStatus.PREPARING));
        return activeOrders.stream()
                .filter(o -> !o.getId().equals(order.getId()))
                .filter(o -> o.getCreatedAt().isBefore(order.getCreatedAt())
                        || (o.getCreatedAt().equals(order.getCreatedAt()) && o.getId() < order.getId()))
                .sorted(Comparator.comparing(Order::getCreatedAt).thenComparing(Order::getId))
                .toList();
    }

    double calculateWaitMinutes(List<Order> ordersAhead) {
        if (ordersAhead.isEmpty()) {
            return 0;
        }

        List<Double> prepTimes = ordersAhead.stream()
                .map(this::calculateOrderPrepTime)
                .toList();

        int stations = kitchenConfig.stations();
        double totalWait = 0;

        List<Double> remaining = new ArrayList<>(prepTimes);
        while (!remaining.isEmpty()) {
            int batchSize = Math.min(remaining.size(), stations);
            List<Double> batch = remaining.subList(0, batchSize);
            double batchDuration = batch.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            totalWait += batchDuration;
            remaining = new ArrayList<>(remaining.subList(batchSize, remaining.size()));
        }

        return totalWait;
    }
}
