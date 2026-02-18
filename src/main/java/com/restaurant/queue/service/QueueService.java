package com.restaurant.queue.service;

import com.restaurant.queue.config.KitchenConfig;
import com.restaurant.queue.dto.OrderResponse;
import com.restaurant.queue.dto.QueueResponse;
import com.restaurant.queue.entity.Order;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.InvalidOperationException;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueueService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final EstimationService estimationService;
    private final NotificationService notificationService;
    private final KitchenConfig kitchenConfig;

    public QueueService(OrderRepository orderRepository, OrderService orderService,
                        EstimationService estimationService, NotificationService notificationService,
                        KitchenConfig kitchenConfig) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.estimationService = estimationService;
        this.notificationService = notificationService;
        this.kitchenConfig = kitchenConfig;
    }

    public QueueResponse getQueueStatus() {
        List<OrderResponse> pending = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING)
                .stream().map(orderService::toResponse).toList();
        List<OrderResponse> preparing = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PREPARING)
                .stream().map(orderService::toResponse).toList();

        int activeStations = (int) orderRepository.countByStatus(OrderStatus.PREPARING);
        int availableStations = Math.max(0, kitchenConfig.stations() - activeStations);

        return new QueueResponse(pending, preparing, availableStations, kitchenConfig.stations());
    }

    @Transactional
    public OrderResponse processNextOrder() {
        long activeStations = orderRepository.countByStatus(OrderStatus.PREPARING);
        if (activeStations >= kitchenConfig.stations()) {
            throw new InvalidOperationException("All stations are busy. No available stations.");
        }

        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        if (pendingOrders.isEmpty()) {
            throw new ResourceNotFoundException("No pending orders in the queue.");
        }

        Order order = pendingOrders.getFirst();
        order.setStatus(OrderStatus.PREPARING);
        order = orderRepository.save(order);

        estimationService.recalculateAllPendingOrders();

        OrderResponse response = orderService.toResponse(order);
        notificationService.notifyQueue(response);
        notificationService.notifyOrder(order.getId(), response);
        notificationService.notifyKitchen(getQueueStatus());
        return response;
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidOperationException(
                    "Only PREPARING orders can be completed. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.READY);
        order = orderRepository.save(order);

        estimationService.recalculateAllPendingOrders();

        OrderResponse response = orderService.toResponse(order);
        notificationService.notifyQueue(response);
        notificationService.notifyOrder(order.getId(), response);
        notificationService.notifyKitchen(getQueueStatus());
        return response;
    }

    @Transactional
    public OrderResponse deliverOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        if (order.getStatus() != OrderStatus.READY) {
            throw new InvalidOperationException(
                    "Only READY orders can be delivered. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        order = orderRepository.save(order);

        OrderResponse response = orderService.toResponse(order);
        notificationService.notifyQueue(response);
        notificationService.notifyOrder(order.getId(), response);
        return response;
    }
}
