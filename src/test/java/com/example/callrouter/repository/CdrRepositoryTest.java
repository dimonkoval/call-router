package com.example.callrouter.repository;

import com.example.callrouter.model.CallDetailRecord;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        long setupTime = System.currentTimeMillis();
        long startTime = setupTime + 5_000L; // імітуємо момент "answered"

        CallDetailRecord record = new CallDetailRecord();
        record.setCallId(callId);
        record.setFromNumber("userA");
        record.setToNumber("userB");
        record.setSetupTime(setupTime);
        record.setStartTime(startTime);
        record.setStatus("in_progress"); // тепер — in_progress

        cdrRepository.save(record);
        entityManager.flush();
        entityManager.clear();

        // when
        long endTime = startTime + 10_000L; // імітуємо момент "completed"
        long duration = endTime - startTime;

        int updatedCount = cdrRepository.markCompleted(callId, endTime, duration);

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(updatedCount)
                .as("")
                .isEqualTo(1);

        Optional<CallDetailRecord> updated = cdrRepository.findByCallId(callId);
        assertThat(updated)
                .as(" callId in BD")
                .isPresent();

        CallDetailRecord c = updated.get();
        assertThat(c.getEndTime())
                .as("After markCompleted endTime will match")
                .isEqualTo(endTime);
        assertThat(c.getDuration())
                .as("After markCompleted duration will be endTime - startTime")
                .isEqualTo(duration);
        assertThat(c.getStatus())
                .as("Status will be 'completed'")
                .isEqualTo("completed");
    }
}
