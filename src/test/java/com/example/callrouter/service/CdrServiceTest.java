package com.example.callrouter.service;

import com.example.callrouter.model.CallDetailRecord;
import com.example.callrouter.repository.CdrRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class CdrServiceTest {

    @Mock
    private CdrRepository repo;

    @InjectMocks
    private CdrService cdrService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOnInvite_RecordDoesNotExist_SavesNewRecord() {
        // given
        String callId = "abc123";
        String from = "userA";
        String to = "userB";
        long setupTime = System.currentTimeMillis();

        when(repo.findByCallIdAndStatus(callId, "in_progress"))
                .thenReturn(Optional.empty());

        // when
        cdrService.onInvite(callId, from, to, setupTime, null);

        // then
        verify(repo).save(argThat(record ->
                record.getCallId().equals(callId)
                        && record.getFromNumber().equals(from)
                        && record.getToNumber().equals(to)
                        && record.getSetupTime() == setupTime
                        && record.getStartTime() == 0
                        && record.getEndTime() == 0
                        && record.getDuration() == 0
                        && record.getStatus().equals("in_progress")
        ));
    }

    @Test
    void testOnInvite_RecordExists_DoesNothing() {
        // given
        String callId = "abc123";
        when(repo.findByCallIdAndStatus(callId, "in_progress"))
                .thenReturn(Optional.of(mock(CallDetailRecord.class)));

        // when
        cdrService.onInvite(callId, "userA", "userB", System.currentTimeMillis(), null);

        // then
        verify(repo, never()).save(any());
    }
    @Test
    void testOnBye_RecordExists_CallsUpdateStatus() {
        // given
        String callId = "abc123";
        long setupTime = System.currentTimeMillis() - 20_000;
        long startTime = setupTime + 10_000;
        long endTime = startTime + 5_000;
        long expectedDuration = endTime - startTime;

        CallDetailRecord record = new CallDetailRecord();
        record.setCallId(callId);
        record.setStartTime(startTime);
        record.setStatus("answered");

        when(repo.findByCallIdAndStatus(callId, "answered"))
                .thenReturn(Optional.of(record));
        when(repo.updateStatus(
                eq(callId),
                eq("answered"),
                eq("completed"),
                eq(false),
                isNull(),
                eq(true),
                eq(endTime),
                eq(true),
                eq(expectedDuration)
        )).thenReturn(1);

        // when
        cdrService.onBye(callId, endTime);

        // then
        verify(repo).updateStatus(
                eq(callId),
                eq("answered"),
                eq("completed"),
                eq(false),
                isNull(),
                eq(true),
                eq(endTime),
                eq(true),
                eq(expectedDuration)
        );
    }

    @Test
    void testOnBye_RecordNotFound_ThrowsException() {
        // given
        String callId = "missing-call";
        // коли немає запису зі статусом "answered"
        when(repo.findByCallIdAndStatus(callId, "answered"))
                .thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> cdrService.onBye(callId, System.currentTimeMillis()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CDR not found or not answered for callId: " + callId);
    }

    @Test
    void testOnBye_UpdateFailed_ThrowsException() {
        // given
        String callId = "will-fail";
        long startTime = System.currentTimeMillis() - 10_000;
        long endTime = startTime + 1_000;

        CallDetailRecord record = new CallDetailRecord();
        record.setCallId(callId);
        record.setStartTime(startTime);
        record.setStatus("answered");

        when(repo.findByCallIdAndStatus(callId, "answered"))
                .thenReturn(Optional.of(record));
        // змушуємо updateStatus повернути 0 — імітація збою оновлення
        when(repo.updateStatus(
                anyString(), anyString(), anyString(),
                anyBoolean(), any(), anyBoolean(), any(), anyBoolean(), any()
        )).thenReturn(0);

        // when/then
        assertThatThrownBy(() -> cdrService.onBye(callId, endTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update CDR for callId: " + callId);
    }
}
