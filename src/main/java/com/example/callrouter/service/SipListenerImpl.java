package com.example.callrouter.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

@Component
public class SipListenerImpl implements SipListener {
    private final RegisterService registerService;
    private final InviteService inviteService;

    public SipListenerImpl(RegisterService registerService, @Lazy InviteService inviteService) {
        this.registerService = registerService;
        this.inviteService = inviteService;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        String method = request.getMethod();
        switch (method) {
            case Request.REGISTER:
                registerService.handle(requestEvent);
                break;
            case Request.INVITE:
                inviteService.handle(requestEvent);
                break;
            default:
                // respond with 405 Method Not Allowed
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
