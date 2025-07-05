package com.example.callrouter.controller;

import com.example.callrouter.service.CallMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MetricsController {
    private final CallMetricsService metrics;
    public MetricsController(CallMetricsService metrics) { this.metrics = metrics; }
    @GetMapping("/metrics/calls")
    public Map<String, Object> getMetrics() {
        return Map.of(
                "active", metrics.activeCalls(),
                "averageDuration", metrics.averageDuration()
        );
    }
}
