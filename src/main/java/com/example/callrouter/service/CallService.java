package com.example.callrouter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.time.Duration;

import static com.example.callrouter.util.TimestampUtil.extractTimestamp;


@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {
    private static final String REDIS_KEY_PREFIX = "call:";

    private final RedisTemplate<String, String> redis;
    private final MessageFactory messageFactory;
    private final CdrService cdrService;
    private final CallMetricsService metricsService;

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();
        String callId = ((CallIdHeader) req.getHeader(CallIdHeader.NAME)).getCallId();
        String byeKey = "bye:" + callId;

        Boolean firstBye = redis.opsForValue().setIfAbsent(byeKey, "1");
        if (!Boolean.TRUE.equals(firstBye)) {
            log.warn("Repeat BYE for {} ignoring", callId);
            sendOk(evt);
            return;
        }
        redis.expire(byeKey, Duration.ofMinutes(5));

        String callKey = "call:" + callId;
        String startStr = redis.opsForValue().get(callKey);
        if (startStr != null) {
            redis.delete(callKey);
        }

        long startTime = startStr != null ? Long.parseLong(startStr) : extractTimestamp(req);
        long endTime   = extractTimestamp(req);
        long duration  = Math.max(0, endTime - startTime);

        cdrService.onBye(callId, endTime);
        metricsService.decrementCalls();
        metricsService.addDuration(duration);
        log.debug("Call completed: {} ({} ms)", callId, duration);
        sendOk(evt);
    }


    private void sendErrorResponse(RequestEvent evt, int statusCode) {
        try {
            Response error = messageFactory.createResponse(statusCode, evt.getRequest());
            evt.getServerTransaction().sendResponse(error);
        } catch (Exception ex) {
            log.error("Failure to send error response", ex);
        }
    }

    private void sendOk(RequestEvent evt) {
        try {
            Response ok = messageFactory.createResponse(Response.OK, evt.getRequest());
            evt.getServerTransaction().sendResponse(ok);
        } catch (Exception e) {
            log.error("Failed to send OK", e);
        }
    }
}