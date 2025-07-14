package com.example.callrouter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cdr")
@Getter
@Setter
@NoArgsConstructor
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
    private long startTime;

    @Column(nullable = false)
    private long endTime;

    @Column(nullable = false)
    private long duration;

    @Column(nullable = false)
    private String status;

    public CallDetailRecord(String callId, String fromNumber, String toNumber,
                            long startTime, long endTime, long duration, String status) {
        this.callId = callId;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.status = status;
    }
}