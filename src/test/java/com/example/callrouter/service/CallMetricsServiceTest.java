package com.example.callrouter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallMetricsServiceTest {

    private CallMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new CallMetricsService();
    }

    @Test
    void testIncrementAndDecrementCalls() {
        metricsService.incrementCalls();
        metricsService.incrementCalls();
        assertThat(metricsService.getActiveCalls()).isEqualTo(2);

        metricsService.decrementCalls();
        assertThat(metricsService.getActiveCalls()).isEqualTo(1);
    }

    @Test
    void testAddDurationAndCompletedCalls() {
        metricsService.addDuration(1000);
        metricsService.addDuration(3000);

        assertThat(metricsService.getTotalDuration()).isEqualTo(4000);
        assertThat(metricsService.getCompletedCalls()).isEqualTo(2);
        assertThat(metricsService.getAverageDuration()).isEqualTo(2000.0);
    }

    @Test
    void testGetAverageDurationWhenNoCallsCompleted() {
        assertThat(metricsService.getAverageDuration()).isEqualTo(0.0);
    }
}
