package com.example.callrouter.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class CallServiceTest {

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
    private ServerTransaction serverTransaction;

    @Mock
    private Response response;

    @InjectMocks
    private CallService callService;

    private final String callId = "test-call-123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // opsForValue() → valueOps
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // перший setIfAbsent → true (тобто перший BYE обробляється)
        when(valueOps.setIfAbsent("bye:" + callId, "1")).thenReturn(true);

        // expire(...) повертає true
        when(redisTemplate.expire(eq("bye:" + callId), any(Duration.class)))
                .thenReturn(true);
    }


    @Test
    void testHandleByeRequest_FirstBye_CallsOnBye() throws Exception {
        // --- підготовка даних для RequestEvent/Request
        when(requestEvent.getRequest()).thenReturn(request);
        when(requestEvent.getServerTransaction()).thenReturn(serverTransaction);

        // заглушка заголовка Call-ID
        when(request.getHeader(CallIdHeader.NAME)).thenReturn(callIdHeader);
        when(callIdHeader.getCallId()).thenReturn(callId);

        // Redis: повертаємо рядок із часом старту
        long fakeStart = System.currentTimeMillis() - 1_000;
        when(valueOps.getAndDelete("call:" + callId)).thenReturn(String.valueOf(fakeStart));

        // при створенні Response.OK
        when(messageFactory.createResponse(Response.OK, request)).thenReturn(response);

        // виклик методу
        callService.handle(requestEvent);

        // перевіряємо, що onBye пішов у сервіс
        verify(cdrService).onBye(eq(callId), anyLong());

        // перевіряємо, що лічильники знизилися і додано тривалість
        verify(metricsService).decrementCalls();
        verify(metricsService).addDuration(anyLong());

        // і відповіли OK
        verify(serverTransaction).sendResponse(response);
    }

    @Test
    void testHandleByeRequest_SecondBye_Ignored() throws Exception {
        // на другий виклик setIfAbsent повертаємо false
        when(valueOps.setIfAbsent("bye:" + callId, "1")).thenReturn(false);

        when(requestEvent.getRequest()).thenReturn(request);
        when(requestEvent.getServerTransaction()).thenReturn(serverTransaction);
        when(request.getHeader(CallIdHeader.NAME)).thenReturn(callIdHeader);
        when(callIdHeader.getCallId()).thenReturn(callId);
        when(messageFactory.createResponse(Response.OK, request)).thenReturn(response);

        // перший виклик
        callService.handle(requestEvent);
        // очистимо інвокації
        reset(cdrService, metricsService, serverTransaction);

        // другий виклик
        callService.handle(requestEvent);

        // переконаємося, що onBye не виконується вдруге
        verifyNoInteractions(cdrService, metricsService);
        // але OK надсилається
        verify(serverTransaction).sendResponse(response);
    }
}
