package com.restaurant.queue.controller;

import com.restaurant.queue.dto.DishRequest;
import com.restaurant.queue.dto.DishResponse;
import com.restaurant.queue.service.DishService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public List<DishResponse> getAllDishes() {
        return dishService.getAllActiveDishes();
    }

    @GetMapping("/{id}")
    public DishResponse getDish(@PathVariable Long id) {
        return dishService.getDishById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse createDish(@Valid @RequestBody DishRequest request) {
        return dishService.createDish(request);
    }

    @PutMapping("/{id}")
    public DishResponse updateDish(@PathVariable Long id, @Valid @RequestBody DishRequest request) {
        return dishService.updateDish(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
    }
}
