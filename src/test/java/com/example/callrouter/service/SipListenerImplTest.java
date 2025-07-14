package com.example.callrouter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import static org.mockito.Mockito.*;

class SipListenerImplTest {

    @Mock private RegisterService registerService;
    @Mock private InviteService inviteService;
    @Mock private CallService callService;
    @Mock private  CdrService cdrService;
    @Mock private RequestEvent requestEvent;
    @Mock private Request request;
    @Mock private CallIdHeader callIdHeader;

    private SipListenerImpl listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new SipListenerImpl(registerService, inviteService, callService, cdrService);

        when(requestEvent.getRequest()).thenReturn(request);
        when(request.getHeader(CallIdHeader.NAME)).thenReturn(callIdHeader);
        when(callIdHeader.getCallId()).thenReturn("test-call-id");
    }

    @Test
    void processRequest_register_shouldDelegateToRegisterService() {
        when(request.getMethod()).thenReturn(Request.REGISTER);

        listener.processRequest(requestEvent);

        verify(registerService).handle(requestEvent);
        verifyNoInteractions(inviteService, callService);
    }

    @Test
    void processRequest_invite_shouldDelegateToInviteService() {
        when(request.getMethod()).thenReturn(Request.INVITE);

        listener.processRequest(requestEvent);

        verify(inviteService).handle(requestEvent);
        verifyNoInteractions(registerService, callService);
    }

    @Test
    void processRequest_bye_shouldDelegateToCallService() {
        when(request.getMethod()).thenReturn(Request.BYE);

        listener.processRequest(requestEvent);

        verify(callService).handle(requestEvent);
        verifyNoInteractions(registerService, inviteService);
    }

    @Test
    void processRequest_unknownMethod_shouldCallReject() {
        when(request.getMethod()).thenReturn("OPTIONS");

        listener.processRequest(requestEvent);

        verify(inviteService).reject(requestEvent, Response.METHOD_NOT_ALLOWED);
        verifyNoInteractions(registerService, callService);
    }
}
