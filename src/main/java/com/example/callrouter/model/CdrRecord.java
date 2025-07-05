package com.example.callrouter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity @Table(name = "cdr")
@Data
public class CdrRecord {
    @Id private String callId;
    private String caller;
    private String callee;
    private Long duration;
    public CdrRecord() {}
    public CdrRecord(String id, String from, String to) {
        this.callId = id; this.caller = from; this.callee = to;
    }
}
