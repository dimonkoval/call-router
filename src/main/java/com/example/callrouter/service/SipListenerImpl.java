package com.example.callrouter.service;

import static com.example.callrouter.util.TimestampUtil.extractTimestamp;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sip.header.CallIdHeader;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.DateHeader;
import javax.sip.header.Header;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SipListenerImpl implements SipListener {
    private final RegisterService registerService;
    private final InviteService inviteService;
    private final CallService callService;
    private final CdrService cdrService;
    private final Map<String, Integer> callStatuses = new ConcurrentHashMap<>();

    public SipListenerImpl(RegisterService registerService, @Lazy InviteService inviteService, CallService callService, CdrService cdrService) {
        this.registerService = registerService;
        this.inviteService = inviteService;
        this.callService = callService;
        this.cdrService = cdrService;
    }

    @Scheduled(fixedRate = 300_000) // Кожні 5 хвилин
    public void cleanupProcessedResponses() {
        int initialSize = callStatuses.size();
        callStatuses.keySet().removeIf(key ->
                System.currentTimeMillis() - Long.parseLong(key.split("-")[0]) > 3600_000); // 1 година
        log.debug("Cleaned up call statuses: {} -> {}", initialSize, callStatuses.size());
    }


    @Override
    public void processRequest(RequestEvent requestEvent) {
        log.info(">>> REQUEST: {}", requestEvent.getRequest());
        Request request = requestEvent.getRequest();
        String method = request.getMethod();
        String callId = ((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId();
        long ts = extractTimestamp(request);
        switch (method) {
            case Request.REGISTER:
                log.debug(">>> REGISTER received in Listener, callId={}", callId);
                registerService.handle(requestEvent);
                break;
            case Request.INVITE:
                log.debug(">>> INVITE received in Listener, callId={}", callId);
                inviteService.handle(requestEvent);
                break;
            case Request.BYE:
                log.debug(">>> BYE received in Listener, callId={}", callId);
                callService.handle(requestEvent);
                break;
            case Request.CANCEL:
                log.debug(">>> CANCEL received, callId={}", callId);
                cdrService.onMissed(callId, extractTimestamp(request));
                break;
            default:
                inviteService.reject(requestEvent, Response.METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response resp = responseEvent.getResponse();
        int status = resp.getStatusCode();
        String callId = ((CallIdHeader) resp.getHeader(CallIdHeader.NAME)).getCallId();
        long timestamp = extractTimestamp(resp);
        log.debug(">>> STATUS: {} for call {}", status, callId);
        // Ігноруємо проміжні статуси (1xx)
        if (status >= 100 && status < 200) return;

        // Обробка 200 OK - найвищий пріоритет
        if (status == Response.OK) {
            log.debug(">>> 200 OK for call {}, force ANSWERED", callId);
            callStatuses.put(callId, Response.OK);
            cdrService.onAnswered(callId, timestamp);
            return;
        }

        // Якщо вже отримали 200 OK - ігноруємо інші статуси
        if (callStatuses.get(callId) != null && callStatuses.get(callId) == Response.OK) {
            log.debug(">>> Ignoring response {} for answered call {}", status, callId);
            return;
        }

        if ((status == Response.BUSY_HERE || status >= 400) && status < 600) {
            if (callStatuses.putIfAbsent(callId, status) == null) {
                String statusName = "MISSED";
                if (status == Response.BUSY_HERE) statusName = "MISSED";

                log.debug(">>> {} ({}) for call {}, marking as MISSED",
                        status, statusName, callId);
                cdrService.onMissed(callId, timestamp);
            }
        }
        // Обробка відмов (включаючи 486 Busy та 603 Decline)
        if (status == Response.DECLINE ||  status == 603) {
//            if (callStatuses.putIfAbsent(callId, status) == null) {
                String statusName = "REJECTED";
                if (status == Response.DECLINE) statusName = "REJECTED";

                log.debug(">>> {} ({}) for call {}, marking as REJECTED",
                        status, statusName, callId);
                cdrService.onReject(callId, timestamp);
//            }
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
