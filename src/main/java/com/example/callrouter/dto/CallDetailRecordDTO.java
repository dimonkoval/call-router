package com.example.callrouter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallDetailRecordDTO {
    private String callId;
    private String from;
    private String to;
    private long start;
    private long end;
    private long duration;
}
