package com.restaurant.queue.entity;

import com.restaurant.queue.enums.DishCategory;
import com.restaurant.queue.enums.DishComplexity;
import jakarta.persistence.*;

@Entity
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishCategory category;

    @Column(nullable = false)
    private int basePreparationTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishComplexity complexity;

    @Column(nullable = false)
    private boolean active = true;

    public Dish() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DishCategory getCategory() { return category; }
    public void setCategory(DishCategory category) { this.category = category; }

    public int getBasePreparationTimeMinutes() { return basePreparationTimeMinutes; }
    public void setBasePreparationTimeMinutes(int basePreparationTimeMinutes) { this.basePreparationTimeMinutes = basePreparationTimeMinutes; }

    public DishComplexity getComplexity() { return complexity; }
    public void setComplexity(DishComplexity complexity) { this.complexity = complexity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
