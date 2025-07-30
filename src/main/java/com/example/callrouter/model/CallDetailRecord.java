package com.example.callrouter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cdr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallDetailRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callId;

    @Column(nullable = false)
    private String fromNumber;

    @Column(nullable = false)
    private String toNumber;

    @Column(nullable = false)
    private long setupTime;

    private long startTime;

    private long endTime;

    @Column(nullable = false)
    private long duration;

    @Column(nullable = false)
    private String status;

    @Column
    private String fromContactName;

    @Column
    private String toContactName;

    public CallDetailRecord(String callId, String fromNumber, String toNumber,
                            long setupTime, long startTime, long endTime, long duration, String status,
                            String fromContactName, String toContactName) {
        this.callId = callId;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.setupTime = setupTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.status = status;
        this.fromContactName = fromContactName;
        this.toContactName = toContactName;
    }

    @Transient
    public String getFormattedDuration() {
        if (duration <= 0) {
            return "00:00:00";
        }

        long seconds = duration / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}