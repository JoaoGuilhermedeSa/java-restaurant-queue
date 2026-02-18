package com.restaurant.queue.controller;

import com.restaurant.queue.dto.KitchenStatusResponse;
import com.restaurant.queue.service.KitchenStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    private final KitchenStatusService kitchenStatusService;

    public KitchenController(KitchenStatusService kitchenStatusService) {
        this.kitchenStatusService = kitchenStatusService;
    }

    @GetMapping("/status")
    public KitchenStatusResponse getKitchenStatus() {
        return kitchenStatusService.getKitchenStatus();
    }
}
