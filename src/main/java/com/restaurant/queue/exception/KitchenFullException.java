package com.restaurant.queue.exception;

public class KitchenFullException extends RuntimeException {

    public KitchenFullException(String message) {
        super(message);
    }
}
