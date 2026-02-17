package com.restaurant.queue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kitchen")
public record KitchenConfig(
        int stations,
        int maxQueueSize
) {}
