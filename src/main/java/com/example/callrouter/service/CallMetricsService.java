package com.example.callrouter.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CallMetricsService {
    private final AtomicInteger activeCalls   = new AtomicInteger(0);
    private final AtomicLong    totalDuration = new AtomicLong(0);
    private final AtomicLong    completedCalls = new AtomicLong(0);

    public void incrementCalls() {
        activeCalls.incrementAndGet();
    }

    public void decrementCalls() {
        activeCalls.decrementAndGet();
    }

    public void addDuration(long durationMillis) {
        totalDuration.addAndGet(durationMillis);
        completedCalls.incrementAndGet();
    }

    public int    getActiveCalls()   { return activeCalls.get(); }
    public double getAverageDuration() {
        long count = completedCalls.get();
        return count == 0 ? 0.0 : (double) totalDuration.get() / count;
    }

    public long getTotalDuration()    { return totalDuration.get(); }
    public long getCompletedCalls()   { return completedCalls.get(); }
}
