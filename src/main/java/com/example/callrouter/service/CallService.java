package com.example.callrouter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;


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
        log.info("Обробка BYE запиту: {}", req);
        String callId = null;

        try {
            callId = ((CallIdHeader) req.getHeader(CallIdHeader.NAME)).getCallId();
            String from = ((FromHeader) req.getHeader(FromHeader.NAME)).getAddress().getURI().toString();
            String to = ((ToHeader) req.getHeader(ToHeader.NAME)).getAddress().getURI().toString();

            String redisKey = REDIS_KEY_PREFIX + callId;
            String startStr = redis.opsForValue().getAndDelete(redisKey);
            long startTime = startStr != null ? Long.parseLong(startStr) : System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            long duration = Math.max(0, endTime - startTime);

            cdrService.onBye(callId, endTime);

            metricsService.decrementCalls();
            metricsService.addDuration(duration);
            log.debug("Виклик оброблено: активні={}, загальна тривалість={}, завершені={}",
                    metricsService.getActiveCalls(), metricsService.getTotalDuration(), metricsService.getCompletedCalls());

            Response ok = messageFactory.createResponse(Response.OK, req);
            evt.getServerTransaction().sendResponse(ok);

        } catch (Exception e) {
            log.error("Помилка обробки BYE запиту{}", callId != null ? " для callId: " + callId : "", e);
            sendErrorResponse(evt, Response.SERVER_INTERNAL_ERROR);
        }
    }

    private void sendErrorResponse(RequestEvent evt, int statusCode) {
        try {
            Response error = messageFactory.createResponse(statusCode, evt.getRequest());
            evt.getServerTransaction().sendResponse(error);
        } catch (Exception ex) {
            log.error("Невдача відправлення відповіді про помилку", ex);
        }
    }
}