package com.example.callrouter.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import javax.sip.SipProvider;
import javax.sip.TransactionUnavailableException;
import javax.sip.message.MessageFactory;
import javax.sip.message.Response;
import org.springframework.stereotype.Service;
import java.time.Duration;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;
import javax.sip.header.FromHeader;
import javax.sip.header.ContactHeader;

@Service
public class RegisterService {
    private final RedisTemplate<String, String> redis;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;
    private static final Duration TTL = Duration.ofMinutes(30);

    public RegisterService(RedisTemplate<String, String> redis,
                           MessageFactory messageFactory, @Lazy SipProvider sipProvider) {
        this.redis = redis;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    public void handle(RequestEvent evt) {
        Request req = evt.getRequest();
        String userUri = ((FromHeader) req.getHeader(FromHeader.NAME))
                .getAddress().getURI().toString();
        String contactUri = ((ContactHeader) req.getHeader(ContactHeader.NAME))
                .getAddress().getURI().toString();
        String key = "registration:" + userUri;
        redis.opsForValue().set(key, contactUri, TTL);

        ServerTransaction tx = evt.getServerTransaction();

        if (tx == null) {
            try {

                if (req.getMethod().equals(Request.REGISTER) && evt.getDialog() == null) {
                    tx = sipProvider.getNewServerTransaction(req);
                } else {
                    throw new IllegalStateException("No transaction for non-initial request");
                }
            } catch (TransactionUnavailableException | javax.sip.TransactionAlreadyExistsException e) {
                throw new RuntimeException("Cannot create transaction for REGISTER", e);
            }
        }

        try {
            Response ok = messageFactory.createResponse(Response.OK, req);
            tx.sendResponse(ok);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send 200 OK for REGISTER", e);
        }
    }
}

