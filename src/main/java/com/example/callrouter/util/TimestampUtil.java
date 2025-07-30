package com.example.callrouter.util;

import javax.sip.header.DateHeader;
import javax.sip.header.Header;
import javax.sip.message.Message;

public final class TimestampUtil {
    public static long extractTimestamp(Message msg) {
        // Спроба отримати з DateHeader (RFC 3261)
        Header dateH = msg.getHeader(DateHeader.NAME);
        if (dateH instanceof DateHeader) {
            try {
                return ((DateHeader) dateH).getDate().getTimeInMillis();
            } catch (Exception ignore) {}
        }

        // Спроба отримати з кастомного заголовка (очікуємо epoch-час у мс)
        Header tsH = msg.getHeader("Timestamp");
        if (tsH != null) {
            try {
                return Long.parseLong(tsH.toString().trim());
            } catch (NumberFormatException ignore) {}
        }

        // Fallback: системний час у мілісекундах (epoch)
        return System.currentTimeMillis();
    }
}