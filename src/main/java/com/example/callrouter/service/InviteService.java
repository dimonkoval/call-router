package com.example.callrouter.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {
    private final RedisTemplate<String, String> redis;
    private final MessageDispatcher proxy;
    private final MessageFactory messageFactory;
    private final CallMetricsService metricsService;
    private final CdrService cdrService;

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();

        String callId = ((CallIdHeader) req.getHeader(CallIdHeader.NAME)).getCallId();
        String from = ((FromHeader) req.getHeader(FromHeader.NAME)).getAddress().getURI().toString();
        String to = ((ToHeader) req.getHeader(ToHeader.NAME)).getAddress().getURI().toString();
        long   start  = System.currentTimeMillis();

        cdrService.onInvite(callId, from, to, start);
        String calleeUri = ((ToHeader)req.getHeader(ToHeader.NAME))
                .getAddress().getURI().toString();
        String key = "registration:" + calleeUri;
        String nextHop = redis.opsForValue().get(key);
        if (nextHop == null) {
            cdrService.onReject(callId, System.currentTimeMillis());
            reject(evt, Response.NOT_FOUND);
        } else {
            callId = ((CallIdHeader)req.getHeader(CallIdHeader.NAME)).getCallId();
            redis.opsForValue().set("call:" + callId, String.valueOf(System.currentTimeMillis()));
            log.debug(">>> INVITE received, callId = {}", callId);
            metricsService.incrementCalls();
            log.debug(">>> activeCalls after increment = {}", metricsService.getActiveCalls());
            proxy.proxyRequest(evt, nextHop);
        }
    }

    public void reject(RequestEvent evt, int status) {
        try {
            String callId = ((CallIdHeader) evt.getRequest()
                    .getHeader(CallIdHeader.NAME))
                    .getCallId();
            long ts = System.currentTimeMillis();
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

