package com.restaurant.queue.service;

import com.restaurant.queue.config.KitchenConfig;
import com.restaurant.queue.dto.*;
import com.restaurant.queue.entity.Dish;
import com.restaurant.queue.entity.Order;
import com.restaurant.queue.entity.OrderItem;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.InvalidOperationException;
import com.restaurant.queue.exception.KitchenFullException;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DishService dishService;
    private final EstimationService estimationService;
    private final NotificationService notificationService;
    private final KitchenConfig kitchenConfig;

    public OrderService(OrderRepository orderRepository, DishService dishService,
                        EstimationService estimationService, NotificationService notificationService,
                        KitchenConfig kitchenConfig) {
        this.orderRepository = orderRepository;
        this.dishService = dishService;
        this.estimationService = estimationService;
        this.notificationService = notificationService;
        this.kitchenConfig = kitchenConfig;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        long queueSize = orderRepository.countByStatus(OrderStatus.PENDING)
                + orderRepository.countByStatus(OrderStatus.PREPARING);
        if (queueSize >= kitchenConfig.maxQueueSize()) {
            throw new KitchenFullException("Kitchen queue is full. Maximum capacity: " + kitchenConfig.maxQueueSize());
        }

        Order order = new Order();
        order.setCustomerName(request.customerName());

        for (OrderItemRequest itemReq : request.items()) {
            Dish dish = dishService.findDishOrThrow(itemReq.dishId());
            OrderItem item = new OrderItem();
            item.setDish(dish);
            item.setQuantity(itemReq.quantity());
            item.setNotes(itemReq.notes());
            order.addItem(item);
        }

        order = orderRepository.save(order);

        EstimateDetails estimate = estimationService.calculateEstimateDetails(order);
        order.setEstimatedCompletionTime(estimate.estimatedCompletionTime());
        order = orderRepository.save(order);

        estimationService.recalculateAllPendingOrders();

        OrderResponse response = toResponse(order);
        notificationService.notifyQueue(response);
        notificationService.notifyOrder(order.getId(), response);
        return response;
    }

    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrderOrThrow(id));
    }

    public List<OrderResponse> getOrders(OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtAsc(status);
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrderOrThrow(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException(
                    "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        estimationService.recalculateAllPendingOrders();

        OrderResponse response = toResponse(order);
        notificationService.notifyQueue(response);
        notificationService.notifyOrder(order.getId(), response);
        return response;
    }

    Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        dishService.toResponse(item.getDish()),
                        item.getQuantity(),
                        item.getNotes()
                ))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                items,
                order.getStatus(),
                order.getCreatedAt(),
                order.getEstimatedCompletionTime(),
                order.getStatusUpdatedAt()
        );
    }
}
