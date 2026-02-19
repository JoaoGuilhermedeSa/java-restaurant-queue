package com.restaurant.queue.controller;

import com.restaurant.queue.dto.OrderItemRequest;
import com.restaurant.queue.dto.OrderItemResponse;
import com.restaurant.queue.dto.OrderRequest;
import com.restaurant.queue.dto.OrderResponse;
import com.restaurant.queue.dto.DishResponse;
import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.service.OrderService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void placeOrder_validRequest_returns201() throws Exception {
        OrderRequest request = new OrderRequest("Alice",
                List.of(new OrderItemRequest(1L, 2, "no onions")));
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.PENDING);
        when(orderService.placeOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void placeOrder_emptyItems_returns400() throws Exception {
        String invalidJson = """
                {"customerName": "Alice", "items": []}
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_missingCustomerName_returns400() throws Exception {
        String invalidJson = """
                {"customerName": "", "items": [{"dishId": 1, "quantity": 1}]}
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_returnsOrder() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.PENDING);
        when(orderService.getOrderById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Alice"));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrders_filterByStatus() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.PENDING);
        when(orderService.getOrders(OrderStatus.PENDING)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void cancelOrder_returns200() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private OrderResponse createOrderResponse(Long id, String name, OrderStatus status) {
        DishResponse dish = new DishResponse(1L, "Bruschetta", "Toast",
                DishCategory.APPETIZER, 5, DishComplexity.SIMPLE, true);
        OrderItemResponse item = new OrderItemResponse(1L, dish, 1, null);
        return new OrderResponse(id, name, List.of(item), status,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), LocalDateTime.now());
    }
}
