package com.example.callrouter.service;

import com.example.callrouter.model.CallDetailRecord;
import com.example.callrouter.repository.CdrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CdrService {
    private final CdrRepository repo;

    @Transactional
    public void onInvite(String callId, String from, String to, long startTime) {

        boolean exists = repo.findByCallIdAndStatus(callId, "in_progress").isPresent();
        if (!exists) {
            CallDetailRecord rec = new CallDetailRecord(
                    callId, from, to, startTime, 0L, 0L, "in_progress"
            );
            repo.save(rec);
        }
    }

    @Transactional
    public void onBye(String callId, long endTime) {

        repo.findByCallIdAndStatus(callId, "in_progress")
                .ifPresentOrElse(rec -> {
                    long duration = endTime - rec.getStartTime();
                    repo.markCompleted(callId, endTime, duration);
                }, () -> {
                    throw new RuntimeException("CDR not found or already completed for callId: " + callId);
                });
    }
}