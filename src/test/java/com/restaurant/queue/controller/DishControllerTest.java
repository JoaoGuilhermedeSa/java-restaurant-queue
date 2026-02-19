package com.restaurant.queue.controller;

import com.restaurant.queue.dto.DishRequest;
import com.restaurant.queue.dto.DishResponse;
import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.service.DishService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DishController.class)
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DishService dishService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllDishes_returnsList() throws Exception {
        DishResponse dish = new DishResponse(1L, "Bruschetta", "Toast", DishCategory.APPETIZER, 5, DishComplexity.SIMPLE, true);
        when(dishService.getAllActiveDishes()).thenReturn(List.of(dish));

        mockMvc.perform(get("/api/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bruschetta"))
                .andExpect(jsonPath("$[0].category").value("APPETIZER"));
    }

    @Test
    void getDish_returnsOne() throws Exception {
        DishResponse dish = new DishResponse(1L, "Bruschetta", "Toast", DishCategory.APPETIZER, 5, DishComplexity.SIMPLE, true);
        when(dishService.getDishById(1L)).thenReturn(dish);

        mockMvc.perform(get("/api/dishes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bruschetta"));
    }

    @Test
    void getDish_notFound_returns404() throws Exception {
        when(dishService.getDishById(999L)).thenThrow(new ResourceNotFoundException("Dish not found with id: 999"));

        mockMvc.perform(get("/api/dishes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dish not found with id: 999"));
    }

    @Test
    void createDish_validRequest_returns201() throws Exception {
        DishRequest request = new DishRequest("New Dish", "Desc", DishCategory.MAIN_COURSE, 15, DishComplexity.MODERATE);
        DishResponse response = new DishResponse(2L, "New Dish", "Desc", DishCategory.MAIN_COURSE, 15, DishComplexity.MODERATE, true);
        when(dishService.createDish(any())).thenReturn(response);

        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Dish"));
    }

    @Test
    void createDish_invalidRequest_returns400() throws Exception {
        String invalidJson = """
                {"name": "", "category": "MAIN_COURSE", "basePreparationTimeMinutes": 0, "complexity": "SIMPLE"}
                """;

        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDish_returns200() throws Exception {
        DishRequest request = new DishRequest("Updated", "Desc", DishCategory.DESSERT, 10, DishComplexity.COMPLEX);
        DishResponse response = new DishResponse(1L, "Updated", "Desc", DishCategory.DESSERT, 10, DishComplexity.COMPLEX, true);
        when(dishService.updateDish(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteDish_returns204() throws Exception {
        mockMvc.perform(delete("/api/dishes/1"))
                .andExpect(status().isNoContent());
    }
}
