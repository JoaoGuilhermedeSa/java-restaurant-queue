package com.restaurant.queue.service;

import com.restaurant.queue.dto.OrderItemRequest;
import com.restaurant.queue.dto.OrderRequest;
import com.restaurant.queue.dto.OrderResponse;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.InvalidOperationException;
import com.restaurant.queue.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private QueueService queueService;

    @Test
    void placeOrder_createsOrderWithPendingStatus() {
        OrderResponse response = placeTestOrder("Alice", 1L, 2);

        assertNotNull(response.id());
        assertEquals("Alice", response.customerName());
        assertEquals(OrderStatus.PENDING, response.status());
        assertNotNull(response.estimatedCompletionTime());
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().getFirst().quantity());
    }

    @Test
    void placeOrder_withMultipleItems() {
        OrderRequest request = new OrderRequest("Bob", List.of(
                new OrderItemRequest(1L, 1, null),
                new OrderItemRequest(2L, 2, "extra crispy")
        ));
        OrderResponse response = orderService.placeOrder(request);

        assertEquals(2, response.items().size());
        assertEquals(OrderStatus.PENDING, response.status());
    }

    @Test
    void placeOrder_withInvalidDish_throws() {
        assertThrows(ResourceNotFoundException.class, () ->
                placeTestOrder("Charlie", 999L, 1));
    }

    @Test
    void getOrderById_returnsOrder() {
        OrderResponse placed = placeTestOrder("Alice", 1L, 1);
        OrderResponse retrieved = orderService.getOrderById(placed.id());

        assertEquals(placed.id(), retrieved.id());
        assertEquals("Alice", retrieved.customerName());
    }

    @Test
    void getOrderById_notFound_throws() {
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(999L));
    }

    @Test
    void cancelOrder_fromPending_succeeds() {
        OrderResponse placed = placeTestOrder("Alice", 1L, 1);
        OrderResponse cancelled = orderService.cancelOrder(placed.id());

        assertEquals(OrderStatus.CANCELLED, cancelled.status());
    }

    @Test
    void cancelOrder_fromPreparing_throws() {
        OrderResponse placed = placeTestOrder("Alice", 1L, 1);
        queueService.processNextOrder();

        assertThrows(InvalidOperationException.class, () ->
                orderService.cancelOrder(placed.id()));
    }

    @Test
    void getOrders_filterByStatus() {
        placeTestOrder("Alice", 1L, 1);
        placeTestOrder("Bob", 2L, 1);

        List<OrderResponse> pending = orderService.getOrders(OrderStatus.PENDING);
        assertEquals(2, pending.size());

        List<OrderResponse> preparing = orderService.getOrders(OrderStatus.PREPARING);
        assertEquals(0, preparing.size());
    }

    @Test
    void orderLifecycle_pendingToDelivered() {
        OrderResponse order = placeTestOrder("Alice", 1L, 1);
        assertEquals(OrderStatus.PENDING, order.status());

        OrderResponse preparing = queueService.processNextOrder();
        assertEquals(OrderStatus.PREPARING, preparing.status());

        OrderResponse ready = queueService.completeOrder(order.id());
        assertEquals(OrderStatus.READY, ready.status());

        OrderResponse delivered = queueService.deliverOrder(order.id());
        assertEquals(OrderStatus.DELIVERED, delivered.status());
    }

    @Test
    void estimatesRecalculated_whenOrderCancelled() {
        OrderResponse first = placeTestOrder("Alice", 1L, 1);
        OrderResponse second = placeTestOrder("Bob", 2L, 1);

        var estimateBefore = orderService.getOrderById(second.id()).estimatedCompletionTime();

        orderService.cancelOrder(first.id());

        var estimateAfter = orderService.getOrderById(second.id()).estimatedCompletionTime();
        assertTrue(estimateAfter.isBefore(estimateBefore) || estimateAfter.isEqual(estimateBefore));
    }

    private OrderResponse placeTestOrder(String customerName, Long dishId, int quantity) {
        OrderRequest request = new OrderRequest(customerName,
                List.of(new OrderItemRequest(dishId, quantity, null)));
        return orderService.placeOrder(request);
    }
}
