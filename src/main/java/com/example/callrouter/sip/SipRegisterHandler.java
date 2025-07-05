package com.example.callrouter.sip;

import com.example.callrouter.model.UserRegistration;
import javax.sip.RequestEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

public class SipRegisterHandler {
    private final RedisTemplate<String, UserRegistration> redis;
    public SipRegisterHandler(RedisTemplate<String, UserRegistration> redis) {
        this.redis = redis;
    }
    public void handle(RequestEvent evt) throws Exception {
        Request req = evt.getRequest();
        ContactHeader ch = (ContactHeader) req.getHeader(ContactHeader.NAME);
        SipURI uri = (SipURI) ch.getAddress().getURI();
        String ip = uri.getHost(); int port = uri.getPort();
        String user = ((SipURI)((FromHeader)req.getHeader(FromHeader.NAME)).getAddress().getURI()).getUser();
        redis.opsForValue().set("user:" + user, new UserRegistration(ip, port), 30, TimeUnit.MINUTES);
    }
}