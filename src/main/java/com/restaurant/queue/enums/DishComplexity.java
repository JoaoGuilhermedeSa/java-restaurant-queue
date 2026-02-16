package com.restaurant.queue.enums;

public enum DishComplexity {
    SIMPLE(1.0),
    MODERATE(1.5),
    COMPLEX(2.0),
    GOURMET(2.5);

    private final double multiplier;

    DishComplexity(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
