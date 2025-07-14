package com.example.callrouter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import javax.sip.header.CallIdHeader;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

@Slf4j
@Component
public class SipListenerImpl implements SipListener {
    private final RegisterService registerService;
    private final InviteService inviteService;
    private final CallService callService;
    private final CdrService cdrService;

    public SipListenerImpl(RegisterService registerService, @Lazy InviteService inviteService, CallService callService, CdrService cdrService) {
        this.registerService = registerService;
        this.inviteService = inviteService;
        this.callService = callService;
        this.cdrService = cdrService;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        log.info(">>> REQUEST: {}", requestEvent.getRequest());
        Request request = requestEvent.getRequest();
        String method = request.getMethod();
        String callId = ((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId();
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
                cdrService.onMissed(callId, System.currentTimeMillis());
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
        long timestamp = System.currentTimeMillis();

        if (status == Response.BUSY_HERE) {          // 486
            log.debug(">>> 486 Busy Here for call {}, marking as MISSED", callId);
            cdrService.onMissed(callId, timestamp);

        } else if (status == Response.DECLINE            // 603
                || (status >= 400 && status < 600)) {
            log.debug(">>> {} received for call {}, marking as REJECTED", status, callId);
            cdrService.onReject(callId, timestamp);

        } else {
            log.trace(">>> Ignoring SIP response {} for call {}", status, callId);
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
