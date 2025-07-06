package com.example.callrouter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sip.message.MessageFactory;
import javax.sip.message.Response;
import org.springframework.stereotype.Service;
import java.time.Duration;                        // ← для TTL
import javax.sip.RequestEvent;                    // ← JAIN-SIP
import javax.sip.ServerTransaction;               // ← для sendResponse
import javax.sip.message.Request;                 // ← JAIN-SIP
import javax.sip.message.Response;                // ← JAIN-SIP
import javax.sip.header.FromHeader;               // ← JAIN-SIP
import javax.sip.header.ContactHeader;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final RedisTemplate<String, String> redis;
    private static final Duration TTL = Duration.ofMinutes(30);
    private final MessageFactory messageFactory;

//    public RegisterService(RedisTemplate<String, String> redis) {
//        this.redis = redis;
//    }

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();
        String userUri = ((FromHeader) req.getHeader(FromHeader.NAME)).getAddress().getURI().toString();
        String contactUri = ((ContactHeader) req.getHeader(ContactHeader.NAME)).getAddress().getURI().toString();
        String key = "registration:" + userUri;
        redis.opsForValue().set(key, contactUri, TTL);
        // send 200 OK
//        Response ok = evt.getDialog().createResponse(Response.OK);
//        sendResponse(evt, ok);
        try {
            ServerTransaction tx = evt.getServerTransaction();
            Response ok = messageFactory.createResponse(Response.OK, req);
            tx.sendResponse(ok);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(RequestEvent evt, Response resp) {
        try {
            evt.getServerTransaction().sendResponse(resp);
        } catch (Exception e) {
            // логування помилки
        }
    }
}
