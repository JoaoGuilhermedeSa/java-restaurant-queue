package com.restaurant.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RestaurantQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantQueueApplication.class, args);
    }
}
