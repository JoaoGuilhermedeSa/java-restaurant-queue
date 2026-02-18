package com.restaurant.queue.service;

import com.restaurant.queue.dto.DishRequest;
import com.restaurant.queue.dto.DishResponse;
import com.restaurant.queue.entity.Dish;
import com.restaurant.queue.exception.ResourceNotFoundException;
import com.restaurant.queue.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<DishResponse> getAllActiveDishes() {
        return dishRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public DishResponse getDishById(Long id) {
        return toResponse(findDishOrThrow(id));
    }

    public DishResponse createDish(DishRequest request) {
        Dish dish = new Dish();
        applyRequest(dish, request);
        return toResponse(dishRepository.save(dish));
    }

    public DishResponse updateDish(Long id, DishRequest request) {
        Dish dish = findDishOrThrow(id);
        applyRequest(dish, request);
        return toResponse(dishRepository.save(dish));
    }

    public void deleteDish(Long id) {
        Dish dish = findDishOrThrow(id);
        dish.setActive(false);
        dishRepository.save(dish);
    }

    Dish findDishOrThrow(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
    }

    private void applyRequest(Dish dish, DishRequest request) {
        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setCategory(request.category());
        dish.setBasePreparationTimeMinutes(request.basePreparationTimeMinutes());
        dish.setComplexity(request.complexity());
        dish.setActive(true);
    }

    public DishResponse toResponse(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getCategory(),
                dish.getBasePreparationTimeMinutes(),
                dish.getComplexity(),
                dish.isActive()
        );
    }
}
