package com.example.callrouter.service;

import static com.example.callrouter.util.TimestampUtil.extractTimestamp;
import com.example.callrouter.service.CallMetricsService;
import com.example.callrouter.sip.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {
    private static final String REDIS_KEY_PREFIX = "call:";
    private static final String REDIS_BYE_PREFIX = "bye:";

    private final RedisTemplate<String, String> redis;
    private final MessageDispatcher proxy;
    private final MessageFactory messageFactory;
    private final CallMetricsService metricsService;
    private final CdrService cdrService;

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();
        String callId = ((CallIdHeader) req.getHeader(CallIdHeader.NAME)).getCallId();

        // 1. Записуємо старт лише один раз
        String redisKey = REDIS_KEY_PREFIX + callId;
        long startTs = extractTimestamp(req);
        Boolean firstInvite = redis.opsForValue().setIfAbsent(redisKey, String.valueOf(startTs));
        if (Boolean.TRUE.equals(firstInvite)) {
            // TTL, щоб ключ помер через годину, якщо BYE не прийде
            redis.expire(redisKey, Duration.ofHours(1));

            // CDR + метрики тільки при першому записі
            String from = ((FromHeader) req.getHeader(FromHeader.NAME))
                    .getAddress().getURI().toString();
            String to   = ((ToHeader)   req.getHeader(ToHeader.NAME))
                    .getAddress().getURI().toString();
            String fromName = ((FromHeader) req.getHeader(FromHeader.NAME))
                    .getAddress().getDisplayName();

            cdrService.onInvite(callId, from, to, startTs, fromName);
            metricsService.incrementCalls();

            log.debug("Recorded start for call {} at {}", callId, startTs);
        } else {
            log.debug("Duplicate INVITE for call {} ignored", callId);
        }

        // 2. Проксируємо виклик як раніше
        String calleeUri = ((ToHeader) req.getHeader(ToHeader.NAME))
                .getAddress().getURI().toString();
        String nextHop = redis.opsForValue().get("registration:" + calleeUri);

        if (nextHop == null) {
            // reject якщо користувач не зареєстрований
            long ts = extractTimestamp(req);
            cdrService.onReject(callId, ts);
            reject(evt, Response.NOT_FOUND);
        } else {
            try {
                proxy.proxyRequest(evt, nextHop);
            } catch (Exception e) {
                log.error("Proxy failed for call {}", callId, e);
                reject(evt, Response.SERVER_INTERNAL_ERROR);
            }
        }
    }

    public void reject(RequestEvent evt, int status) {
        try {
            String callId = ((CallIdHeader) evt.getRequest()
                    .getHeader(CallIdHeader.NAME))
                    .getCallId();
            long ts = extractTimestamp(evt.getRequest());
            cdrService.onReject(callId, ts);

            ServerTransaction tx = evt.getServerTransaction();
            if (tx == null) {
                tx = proxy.getSipProvider().getNewServerTransaction(evt.getRequest());
            }

            Response resp = messageFactory.createResponse(status, evt.getRequest());
            tx.sendResponse(resp);

        } catch (TransactionUnavailableException e) {
            log.error("Cannot create new server transaction", e);
        } catch (Exception e) {
            log.error("Failed to send response", e);
        }
    }
}

