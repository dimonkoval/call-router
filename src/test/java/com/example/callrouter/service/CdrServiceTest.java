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
        long startTime = System.currentTimeMillis();

        when(repo.findByCallIdAndStatus(callId, "in_progress")).thenReturn(Optional.empty());

        // when
        cdrService.onInvite(callId, from, to, startTime);

        // then
        verify(repo).save(argThat(record ->
                record.getCallId().equals(callId)
                        && record.getFromNumber().equals(from)
                        && record.getToNumber().equals(to)
                        && record.getStartTime() == startTime
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
        cdrService.onInvite(callId, "userA", "userB", System.currentTimeMillis());

        // then
        verify(repo, never()).save(any());
    }

    @Test
    void testOnBye_RecordExists_CallsMarkCompleted() {
        // given
        String callId = "abc123";
        long startTime = System.currentTimeMillis() - 10000;
        long endTime = System.currentTimeMillis();

        CallDetailRecord record = new CallDetailRecord(
                callId, "from", "to", startTime, 0L, 0L, "in_progress"
        );

        when(repo.findByCallIdAndStatus(callId, "in_progress")).thenReturn(Optional.of(record));

        // when
        cdrService.onBye(callId, endTime);

        // then
        verify(repo).markCompleted(eq(callId), eq(endTime), eq(endTime - startTime));
    }

    @Test
    void testOnBye_RecordNotFound_ThrowsException() {
        // given
        String callId = "missing-call";
        when(repo.findByCallIdAndStatus(callId, "in_progress")).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> cdrService.onBye(callId, System.currentTimeMillis()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CDR not found or already completed");
    }
}
