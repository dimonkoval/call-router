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

    public SipListenerImpl(RegisterService registerService, @Lazy InviteService inviteService, CallService callService) {
        this.registerService = registerService;
        this.inviteService = inviteService;
        this.callService = callService;
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
            default:
                inviteService.reject(requestEvent, Response.METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

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
