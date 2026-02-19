package com.restaurant.queue.controller;

import com.restaurant.queue.dto.*;
import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import com.restaurant.queue.enums.OrderStatus;
import com.restaurant.queue.exception.InvalidOperationException;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.service.QueueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueueController.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueService queueService;

    @Test
    void getQueue_returnsQueueStatus() throws Exception {
        QueueResponse response = new QueueResponse(List.of(), List.of(), 4, 4);
        when(queueService.getQueueStatus()).thenReturn(response);

        mockMvc.perform(get("/api/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableStations").value(4))
                .andExpect(jsonPath("$.totalStations").value(4))
                .andExpect(jsonPath("$.pendingOrders").isEmpty())
                .andExpect(jsonPath("$.preparingOrders").isEmpty());
    }

    @Test
    void processNext_returnsPreparingOrder() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.PREPARING);
        when(queueService.processNextOrder()).thenReturn(response);

        mockMvc.perform(patch("/api/queue/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    void processNext_emptyQueue_returns404() throws Exception {
        when(queueService.processNextOrder())
                .thenThrow(new ResourceNotFoundException("No pending orders in the queue."));

        mockMvc.perform(patch("/api/queue/next"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processNext_allStationsBusy_returns409() throws Exception {
        when(queueService.processNextOrder())
                .thenThrow(new InvalidOperationException("All stations are busy. No available stations."));

        mockMvc.perform(patch("/api/queue/next"))
                .andExpect(status().isConflict());
    }

    @Test
    void completeOrder_returnsReadyOrder() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.READY);
        when(queueService.completeOrder(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/queue/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void completeOrder_invalidState_returns409() throws Exception {
        when(queueService.completeOrder(1L))
                .thenThrow(new InvalidOperationException("Only PREPARING orders can be completed."));

        mockMvc.perform(patch("/api/queue/1/complete"))
                .andExpect(status().isConflict());
    }

    @Test
    void deliverOrder_returnsDeliveredOrder() throws Exception {
        OrderResponse response = createOrderResponse(1L, "Alice", OrderStatus.DELIVERED);
        when(queueService.deliverOrder(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/queue/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    private OrderResponse createOrderResponse(Long id, String name, OrderStatus status) {
        DishResponse dish = new DishResponse(1L, "Bruschetta", "Toast",
                DishCategory.APPETIZER, 5, DishComplexity.SIMPLE, true);
        OrderItemResponse item = new OrderItemResponse(1L, dish, 1, null);
        return new OrderResponse(id, name, List.of(item), status,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), LocalDateTime.now());
    }
}
