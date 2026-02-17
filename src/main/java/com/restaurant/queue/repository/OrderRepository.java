package com.restaurant.queue.repository;

import com.restaurant.queue.entity.Order;
import com.restaurant.queue.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    long countByStatus(OrderStatus status);
}
