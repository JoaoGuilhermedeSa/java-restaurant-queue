package com.restaurant.queue.service;

import com.restaurant.queue.config.KitchenConfig;
import com.restaurant.queue.dto.EstimateDetails;
import com.restaurant.queue.entity.Dish;
import com.restaurant.queue.entity.Order;
import com.restaurant.queue.entity.OrderItem;
import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstimationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private EstimationService estimationService;

    @BeforeEach
    void setUp() {
        KitchenConfig config = new KitchenConfig(4, 50);
        estimationService = new EstimationService(config, orderRepository);
    }

    @Test
    void calculateBaseTime_singleSimpleItem() {
        Order order = createOrder(1L, createDish(10, DishComplexity.SIMPLE), 1);
        double baseTime = estimationService.calculateBaseTime(order);
        assertEquals(10.0, baseTime);
    }

    @Test
    void calculateBaseTime_multipleQuantity() {
        Order order = createOrder(1L, createDish(10, DishComplexity.SIMPLE), 3);
        double baseTime = estimationService.calculateBaseTime(order);
        assertEquals(30.0, baseTime);
    }

    @Test
    void calculateComplexityAdjustedTime_moderateComplexity() {
        Order order = createOrder(1L, createDish(10, DishComplexity.MODERATE), 1);
        double adjusted = estimationService.calculateComplexityAdjustedTime(order);
        assertEquals(15.0, adjusted); // 10 * 1.5
    }

    @Test
    void calculateComplexityAdjustedTime_gourmetComplexity() {
        Order order = createOrder(1L, createDish(10, DishComplexity.GOURMET), 2);
        double adjusted = estimationService.calculateComplexityAdjustedTime(order);
        assertEquals(50.0, adjusted); // 10 * 2.5 * 2
    }

    @Test
    void calculateOrderPrepTime_withIntraOrderParallelism() {
        Order order = createOrderWithMultipleItems(1L, List.of(
                new ItemSpec(createDish(10, DishComplexity.SIMPLE), 1),
                new ItemSpec(createDish(8, DishComplexity.SIMPLE), 1)
        ));
        // complexityAdjusted = 10*1 + 8*1 = 18, parallelism = min(2, 4) = 2
        double prepTime = estimationService.calculateOrderPrepTime(order);
        assertEquals(9.0, prepTime); // 18 / 2
    }

    @Test
    void estimateWithNoQueue() {
        Order order = createOrder(1L, createDish(10, DishComplexity.SIMPLE), 1);
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(order));

        EstimateDetails details = estimationService.calculateEstimateDetails(order);

        assertEquals(0, details.ordersAhead());
        assertEquals(0.0, details.waitMinutes());
        assertEquals(10.0, details.prepMinutes()); // 10 * 1.0 / min(1, 4)
        assertEquals(10.0, details.totalMinutes());
    }

    @Test
    void estimateWithOrdersAhead_singleBatch() {
        Order ahead1 = createOrder(1L, createDish(10, DishComplexity.SIMPLE), 1);
        ahead1.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        Order ahead2 = createOrder(2L, createDish(15, DishComplexity.SIMPLE), 1);
        ahead2.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        Order current = createOrder(3L, createDish(8, DishComplexity.SIMPLE), 1);
        current.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any()))
                .thenReturn(List.of(ahead1, ahead2, current));

        EstimateDetails details = estimationService.calculateEstimateDetails(current);

        assertEquals(2, details.ordersAhead());
        // ahead1 prep = 10, ahead2 prep = 15, batch of up to 4: max(10, 15) = 15
        assertEquals(15.0, details.waitMinutes());
        assertEquals(8.0, details.prepMinutes());
        assertEquals(23.0, details.totalMinutes());
    }

    @Test
    void estimateWithOrdersAhead_multiBatch() {
        // Create 5 orders ahead (stations = 4, so 2 batches: 4+1)
        List<Order> allOrders = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Order o = createOrder((long) i, createDish(10, DishComplexity.SIMPLE), 1);
            o.setCreatedAt(LocalDateTime.now().minusMinutes(10 - i));
            allOrders.add(o);
        }
        Order current = createOrder(6L, createDish(12, DishComplexity.SIMPLE), 1);
        current.setCreatedAt(LocalDateTime.now());
        allOrders.add(current);

        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(allOrders);

        EstimateDetails details = estimationService.calculateEstimateDetails(current);

        assertEquals(5, details.ordersAhead());
        // batch1: 4 orders of 10min each, max = 10; batch2: 1 order of 10min, max = 10
        assertEquals(20.0, details.waitMinutes());
        assertEquals(12.0, details.prepMinutes());
        assertEquals(32.0, details.totalMinutes());
    }

    @Test
    void estimateWithComplexityMultiplier() {
        Order order = createOrder(1L, createDish(10, DishComplexity.COMPLEX), 1);
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(order));

        EstimateDetails details = estimationService.calculateEstimateDetails(order);

        assertEquals(10.0, details.baseTimeMinutes());
        assertEquals(20.0, details.complexityAdjustedTimeMinutes()); // 10 * 2.0
        assertEquals(20.0, details.prepMinutes());
    }

    private Dish createDish(int baseTime, DishComplexity complexity) {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("Test Dish");
        dish.setCategory(DishCategory.MAIN_COURSE);
        dish.setBasePreparationTimeMinutes(baseTime);
        dish.setComplexity(complexity);
        dish.setActive(true);
        return dish;
    }

    private Order createOrder(Long id, Dish dish, int quantity) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("Test Customer");
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setDish(dish);
        item.setQuantity(quantity);
        item.setOrder(order);
        order.getItems().add(item);

        return order;
    }

    private Order createOrderWithMultipleItems(Long id, List<ItemSpec> specs) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("Test Customer");
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        long itemId = 1;
        for (ItemSpec spec : specs) {
            OrderItem item = new OrderItem();
            item.setId(itemId++);
            item.setDish(spec.dish());
            item.setQuantity(spec.quantity());
            item.setOrder(order);
            order.getItems().add(item);
        }
        return order;
    }

    private record ItemSpec(Dish dish, int quantity) {}
}
