package com.example.callrouter.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.*;

import static org.mockito.Mockito.*;

public class CallServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private MessageFactory messageFactory;

    @Mock
    private CdrService cdrService;

    @Mock
    private CallMetricsService metricsService;

    @Mock
    private RequestEvent requestEvent;

    @Mock
    private Request request;

    @Mock
    private CallIdHeader callIdHeader;

    @Mock
    private FromHeader fromHeader;

    @Mock
    private ToHeader toHeader;

    @Mock
    private Address fromAddress;

    @Mock
    private Address toAddress;

    @Mock
    private URI fromUri;

    @Mock
    private URI toUri;

    @Mock
    private ServerTransaction serverTransaction;

    @Mock
    private Response response;

    @InjectMocks
    private CallService callService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testHandleByeRequest() throws Exception {
        String callId = "test-call-123";
        String startTime = String.valueOf(System.currentTimeMillis() - 1000);

        // Stub headers
        when(request.getHeader(CallIdHeader.NAME)).thenReturn(callIdHeader);
        when(callIdHeader.getCallId()).thenReturn(callId);

        when(request.getHeader(FromHeader.NAME)).thenReturn(fromHeader);
        when(fromHeader.getAddress()).thenReturn(fromAddress);
        when(fromAddress.getURI()).thenReturn(fromUri);
        when(fromUri.toString()).thenReturn("sip:userA@host");

        when(request.getHeader(ToHeader.NAME)).thenReturn(toHeader);
        when(toHeader.getAddress()).thenReturn(toAddress);
        when(toAddress.getURI()).thenReturn(toUri);
        when(toUri.toString()).thenReturn("sip:userB@host");

        // Redis mocks
        when(valueOps.getAndDelete("call:" + callId)).thenReturn(startTime);

        // Request and response
        when(requestEvent.getRequest()).thenReturn(request);
        when(requestEvent.getServerTransaction()).thenReturn(serverTransaction);
        when(messageFactory.createResponse(Response.OK, request)).thenReturn(response);

        // Call method
        callService.handle(requestEvent);

        // Verify interactions
        verify(cdrService).onBye(eq(callId), anyLong());
        verify(metricsService).decrementCalls();
        verify(metricsService).addDuration(anyLong());
        verify(serverTransaction).sendResponse(response);
    }
}
