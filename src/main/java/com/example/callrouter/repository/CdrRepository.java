package com.example.callrouter.repository;

import com.example.callrouter.model.CallDetailRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
       SET c.status = :newStatus,
           c.startTime = CASE WHEN :setStartTime = true THEN :startTime ELSE c.startTime END,
           c.endTime = CASE WHEN :setEndTime = true THEN :endTime ELSE c.endTime END,
           c.duration = CASE WHEN :setDuration = true THEN :duration ELSE c.duration END
     WHERE c.callId = :callId
       AND c.status = :currentStatus
    """)
    int updateStatus(
            @Param("callId") String callId,
            @Param("currentStatus") String currentStatus,
            @Param("newStatus") String newStatus,
            @Param("setStartTime") boolean setStartTime,
            @Param("startTime") Long startTime,
            @Param("setEndTime") boolean setEndTime,
            @Param("endTime") Long endTime,
            @Param("setDuration") boolean setDuration,
            @Param("duration") Long duration
    );

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

    @Query("SELECT c FROM CallDetailRecord c " +
            "WHERE LOWER(c.callId) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR LOWER(c.fromNumber) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR LOWER(c.toNumber) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR LOWER(c.status) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR LOWER(COALESCE(c.fromContactName, '')) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR LOWER(COALESCE(c.toContactName, '')) LIKE LOWER(concat('%', :search, '%')) " +
            "   OR CAST(c.id AS string) LIKE :search") // Add ID to search
    Page<CallDetailRecord> searchCallRecords(@Param("search") String search, Pageable pageable);

    @Modifying
    @Query("""
    UPDATE CallDetailRecord c 
    SET c.status = 'answered',
        c.startTime = :startTime,
        c.endTime = 0,
        c.duration = 0
    WHERE c.callId = :callId 
    AND c.status != 'completed'
""")
    int updateToAnswered(@Param("callId") String callId, @Param("startTime") long startTime);
}
