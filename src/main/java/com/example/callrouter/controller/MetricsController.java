package com.example.callrouter.controller;

import com.example.callrouter.service.CallMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MetricsController {
    private final CallMetricsService metrics;

    @GetMapping("/metrics/calls")
    public Map<String, Object> getMetrics() {
        return Map.of(
                "activeCalls",     metrics.getActiveCalls(),
                "averageDuration", metrics.getAverageDuration()
        );
    }
}
