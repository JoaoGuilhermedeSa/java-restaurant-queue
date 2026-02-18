package com.restaurant.queue.controller;

import com.restaurant.queue.dto.EstimateDetails;
import com.restaurant.queue.service.EstimationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estimates")
public class EstimateController {

    private final EstimationService estimationService;

    public EstimateController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @GetMapping("/{orderId}")
    public EstimateDetails getEstimate(@PathVariable Long orderId) {
        return estimationService.getEstimateDetails(orderId);
    }
}
