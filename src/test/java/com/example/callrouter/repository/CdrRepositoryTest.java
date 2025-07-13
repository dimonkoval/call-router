package com.example.callrouter.repository;

import com.example.callrouter.model.CallDetailRecord;
import com.example.callrouter.repository.CdrRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CdrRepositoryTest {

    @Autowired
    private CdrRepository cdrRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    void testMarkCompleted() {
        // given
        String callId = "test-call-123";
        long startTime = System.currentTimeMillis();

        CallDetailRecord record = new CallDetailRecord(
                callId,
                "userA",
                "userB",
                startTime,
                0L,
                0L,
                "in_progress"
        );

        cdrRepository.save(record);

        // when
        long endTime = startTime + 5000;
        long duration = 5000;
        int updatedCount = cdrRepository.markCompleted(callId, endTime, duration);
        entityManager.flush();
        entityManager.clear();
        // then
        assertThat(updatedCount).isEqualTo(1);

        Optional<CallDetailRecord> updated = cdrRepository.findByCallId(callId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getEndTime()).isEqualTo(endTime);
        assertThat(updated.get().getDuration()).isEqualTo(duration);
        assertThat(updated.get().getStatus()).isEqualTo("completed");
    }
}