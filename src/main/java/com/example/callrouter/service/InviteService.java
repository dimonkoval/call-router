package com.example.callrouter.service;

import com.example.callrouter.sip.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.sip.RequestEvent;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final RedisTemplate<String, String> redis;
    private final MessageDispatcher proxy;
    private final MessageFactory messageFactory;

//    public InviteService(RedisTemplate<String, String> redis, MessageDispatcher proxy) {
//        this.redis = redis;
//        this.proxy = proxy;
//    }

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();
        String calleeUri = ((ToHeader) req.getHeader(ToHeader.NAME)).getAddress().getURI().toString();
        String key = "registration:" + calleeUri;
        String nextHop = redis.opsForValue().get(key);
        if (nextHop == null) {
            reject(evt, Response.NOT_FOUND);
        } else {
            proxy.proxyRequest(evt, nextHop);
        }
    }

    public void reject(RequestEvent evt, int status) {
        try {
            Response resp = messageFactory.createResponse(status, evt.getRequest());
            evt.getServerTransaction().sendResponse(resp);
        } catch (Exception e) {
            e.printStackTrace(); // лог
        }
    }
}

