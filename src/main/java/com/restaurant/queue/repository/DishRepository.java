package com.restaurant.queue.repository;

import com.restaurant.queue.entity.Dish;
import com.restaurant.queue.enums.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByCategory(DishCategory category);

    List<Dish> findByActiveTrue();
}
