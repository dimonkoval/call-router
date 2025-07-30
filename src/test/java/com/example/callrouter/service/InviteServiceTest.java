package com.example.callrouter.service;

import com.example.callrouter.sip.MessageDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InviteServiceTest {

    @Mock private RedisTemplate<String, String> redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private MessageDispatcher proxy;
    @Mock private MessageFactory messageFactory;
    @Mock private CallMetricsService metricsService;
    @Mock private CdrService cdrService;

    @Mock private RequestEvent evt;
    @Mock private Request request;
    @Mock private CallIdHeader callIdHeader;
    @Mock private FromHeader fromHeader;
    @Mock private ToHeader toHeader;
    @Mock private Address fromAddress;
    @Mock private Address toAddress;
    @Mock private javax.sip.address.URI fromUri;
    @Mock private javax.sip.address.URI toUri;

    @InjectMocks private InviteService inviteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(evt.getRequest()).thenReturn(request);
        when(request.getHeader(CallIdHeader.NAME)).thenReturn(callIdHeader);
        when(request.getHeader(FromHeader.NAME)).thenReturn(fromHeader);
        when(request.getHeader(ToHeader.NAME)).thenReturn(toHeader);

        when(callIdHeader.getCallId()).thenReturn("test-call-id");
        when(fromHeader.getAddress()).thenReturn(fromAddress);
        when(toHeader.getAddress()).thenReturn(toAddress);
        when(fromAddress.getURI()).thenReturn(fromUri);
        when(toAddress.getURI()).thenReturn(toUri);
        when(fromUri.toString()).thenReturn("sip:from@test.com");
        when(toUri.toString()).thenReturn("sip:to@test.com");

        when(redis.opsForValue()).thenReturn(valueOps);
    }

//    @Test
//    void testHandle_WithRegisteredCallee() {
//        // given: абонент зареєстрований
//        when(valueOps.get("registration:sip:to@test.com")).thenReturn("127.0.0.1");
//
//        // when
//        inviteService.handle(evt);
//
//        // then: перевіряємо виклик onInvite і всі аргументи-матчери
//        verify(cdrService).onInvite(
//                eq("test-call-id"),
//                eq("sip:from@test.com"),
//                eq("sip:to@test.com"),
//                anyLong(),
//                isNull(String.class)           // <-- використано матчер замість raw null
//        );
//        verify(valueOps).set(startsWith("call:"), anyString());
//        verify(metricsService).incrementCalls();
//        verify(proxy).proxyRequest(eq(evt), eq("127.0.0.1"));
//    }

    @Test
    void testHandle_CalleeNotRegistered_ShouldReject() throws Exception {
        // given: абонент не знайдений
        when(valueOps.get("registration:sip:to@test.com")).thenReturn(null);

        Response mockResp = mock(Response.class);
        ServerTransaction serverTransaction = mock(ServerTransaction.class);
        SipProvider sipProvider = mock(SipProvider.class);

        when(evt.getServerTransaction()).thenReturn(serverTransaction);
        when(messageFactory.createResponse(Response.NOT_FOUND, request)).thenReturn(mockResp);
        when(proxy.getSipProvider()).thenReturn(sipProvider);
        when(sipProvider.getNewServerTransaction(request)).thenReturn(serverTransaction);

        // when
        inviteService.handle(evt);

        // then: відправлено 404, жодних викликів proxy або metricsService
        verify(serverTransaction).sendResponse(mockResp);
        verifyNoInteractions(metricsService);
        verify(proxy, never()).proxyRequest(any(), any());
    }
}
