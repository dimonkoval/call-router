package com.example.callrouter.repository;

import com.example.callrouter.model.CallDetailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CdrRepository extends JpaRepository<CallDetailRecord, Long> {
    Optional<CallDetailRecord> findByCallId(String callId);

    Optional<CallDetailRecord> findByCallIdAndStatus(String callId, String status);

    @Modifying
    @Query("""
    UPDATE CallDetailRecord c
      SET c.endTime = :endTime,
          c.duration = :duration,
          c.status   = 'completed'
    WHERE c.callId = :callId
      AND c.status = 'in_progress'
  """)
    int markCompleted(
            @Param("callId") String callId,
            @Param("endTime") long endTime,
            @Param("duration") long duration
    );

    @Modifying
    @Query("""
      UPDATE CallDetailRecord c
        SET c.endTime = :endTime,
            c.duration = :duration,
            c.status   = 'rejected'
      WHERE c.callId = :callId
        AND c.status = 'in_progress'
    """)
    int markRejected(
            @Param("callId") String callId,
            @Param("endTime") long endTime,
            @Param("duration") long duration
    );

    @Modifying
    @Query("""
      UPDATE CallDetailRecord c
        SET c.endTime = :endTime,
            c.duration = :duration,
            c.status   = 'missed'
      WHERE c.callId = :callId
        AND c.status = 'in_progress'
    """)
    int markMissed(
            @Param("callId") String callId,
            @Param("endTime") long endTime,
            @Param("duration") long duration
    );

    @Modifying
    @Query("""
UPDATE CallDetailRecord c
   SET c.endTime = :timestamp,
       c.duration = :timestamp - c.startTime,
       c.status   = :newStatus
 WHERE c.callId = :callId
   AND c.status = 'in_progress'
""")
    int markStatus(
            @Param("callId") String callId,
            @Param("timestamp") long timestamp,
            @Param("newStatus") String newStatus
    );

}
