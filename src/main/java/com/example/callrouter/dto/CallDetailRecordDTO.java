package com.example.callrouter.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class CallDetailRecordDTO {
    private Long Id;
    private String callId;
    private String from;
    private String to;
    private long setupTime;
    private Long startTime;
    private Long endTime;
    private long duration;
    private String status;
    private String fromContactName;
    private String toContactName;

    public String getFormattedSetupTime() {
        return formatInstant(setupTime, "MMM dd, HH:mm");
    }

    public String getFormattedStartTime() {
        return startTime != null ? formatInstant(startTime, "HH:mm:ss") : "";
    }

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

    private String formatInstant(long millis, String pattern) {
        Instant instant = Instant.ofEpochMilli(millis);
        return DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}