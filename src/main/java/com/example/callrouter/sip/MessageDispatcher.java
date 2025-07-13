package com.example.callrouter.sip;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class MessageDispatcher {

    private final SipProvider sipProvider;
    private final MessageFactory messageFactory;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    @Value("${sip.ip}")
    private String configuredIp;

    @Value("${sip.port}")
    private int configuredPort;

    public void proxyRequest(RequestEvent evt, String nextHopUri) {
        try {
            String bindAddress = configuredIp;
            int bindPort = configuredPort;

            Request original = evt.getRequest();
            Request newRequest = (Request) original.clone();
            newRequest.setRequestURI(addressFactory.createURI(nextHopUri));

            String rrUri = String.format("sip:%s:%d;transport=udp;lr", bindAddress, bindPort);
            javax.sip.address.Address rrAddress = addressFactory.createAddress(rrUri);
            RecordRouteHeader rr = headerFactory.createRecordRouteHeader(rrAddress);
            newRequest.addHeader(rr);

            newRequest.removeHeader(ViaHeader.NAME);
            ViaHeader via = headerFactory.createViaHeader(bindAddress, bindPort, "udp", null);
            newRequest.addHeader(via);

            ClientTransaction tx = sipProvider.getNewClientTransaction(newRequest);
            tx.sendRequest();

        } catch (Exception e) {
            log.error("Proxy INVITE failed", e);
        }
    }

}
