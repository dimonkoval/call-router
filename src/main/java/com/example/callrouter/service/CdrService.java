package com.example.callrouter.service;

import com.example.callrouter.dto.CallDetailRecordDTO;
import com.example.callrouter.mapper.CallDetailRecordMapper;
import com.example.callrouter.model.CallDetailRecord;
import com.example.callrouter.repository.CdrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdrService {
    private final CdrRepository repo;
    private final CallDetailRecordMapper mapper;

    @Transactional
    public void onInvite(String callId, String from, String to, long setupTime, String fromContactName) {
        if (setupTime < 1_000_000_000_000L) { // Перевірка на коректність epoch-часу
            log.warn("Invalid timestamp for call {}: {}. Using current time", callId, setupTime);
            setupTime = System.currentTimeMillis();
        }
        if (!repo.findByCallIdAndStatus(callId, "in_progress").isPresent()) {
            CallDetailRecord rec = new CallDetailRecord();
            rec.setCallId(callId);
            rec.setFromNumber(from);
            rec.setToNumber(to);
            rec.setSetupTime(setupTime);
            rec.setStartTime(0L);
            rec.setEndTime(0L);
            rec.setDuration(0L);
            rec.setStatus("in_progress");
            rec.setFromContactName(fromContactName);

            repo.save(rec);
        }
    }

    @Transactional
    public void onAnswered(String callId, long answerTime) {
        // Оновлюємо будь-який статус (окрім completed) на answered
        int updated = repo.updateToAnswered(callId, answerTime);
        if (updated == 0) {
            log.warn("CDR update failed for answered call: {}", callId);
        }
    }


    @Transactional
    public void onBye(String callId, long endTime) {
        Optional<CallDetailRecord> record = repo.findByCallIdAndStatus(callId, "answered");
        if (record.isEmpty()) {
            throw new RuntimeException("CDR not found or not answered for callId: " + callId);
        }

        long startTime = record.get().getStartTime();
        long duration = endTime - startTime;

        int updated = repo.updateStatus(
                callId,
                "answered",
                "completed",
                false,        // setStartTime
                null,        // startTime
                true,        // setEndTime
                endTime,     // endTime
                true,        // setDuration
                duration     // duration
        );

        if (updated == 0) {
            throw new RuntimeException("Failed to update CDR for callId: " + callId);
        }
    }

    @Transactional
    public void onReject(String callId, long endTime) {
        int updated = repo.updateStatus(
                callId,
                "missed",
                "rejected",  // Загальний статус для всіх видів відмов
                false,       // Не змінюємо startTime
                null,
                true,        // Встановлюємо endTime
                endTime,
                true,        // Встановлюємо duration
                0L           // Тривалість 0
        );

        if (updated == 0) {
            log.warn("CDR not found or wrong status for callId: {}", callId);
        }
    }

    @Transactional
    public void onMissed(String callId, long endTime) {
        int updated = repo.updateStatus(
                callId,
                "in_progress",
                "missed",
                false,       // setStartTime
                null,        // startTime
                true,        // setEndTime
                endTime,    // endTime
                true,       // setDuration
                0L          // duration
        );

        if (updated == 0) {
            throw new RuntimeException("CDR not found or wrong status for callId: " + callId);
        }
    }

    public Optional<CallDetailRecord> getCallRecord(String callId) {
        return repo.findByCallId(callId);
    }

    public Page<CallDetailRecordDTO> getCallRecords(String search, Pageable pageable) {
        Page<CallDetailRecord> entities;

        if (search != null && !search.trim().isEmpty()) {
            entities = repo.searchCallRecords(search, pageable);
        } else {
            entities = repo.findAll(pageable);
        }

        return entities.map(mapper::toDTO);
    }

    @Transactional
    public void onBusy(String callId, long endTime) {
        int updated = repo.updateStatus(
                callId,
                "in_progress",
                "busy", // Новий статус BUSY
                false, null, true, endTime, true, 0L
        );
        if (updated == 0) {
            log.warn("CDR not found for busy call: {}", callId);
        }
    }
}