package com.example.callrouter.sip;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageDispatcher {

    private final SipProvider sipProvider;
    private final MessageFactory messageFactory;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;

    public void proxyRequest(RequestEvent evt, String nextHopUri) {
        try {
            Request originalRequest = evt.getRequest();

            // Створити копію запиту
            Request newRequest = (Request) originalRequest.clone();

            // Змінити Request-URI
            SipURI newUri = (SipURI) addressFactory.createURI(nextHopUri);
            newRequest.setRequestURI(newUri);

            // Обов'язково видалити попередній Via і додати свій (для правильного маршруту)
            newRequest.removeHeader("Via");
            newRequest.addHeader(headerFactory.createViaHeader("localhost", sipProvider.getListeningPoint("udp").getPort(), "udp", null));

            // Створити нову транзакцію
            ClientTransaction transaction = sipProvider.getNewClientTransaction(newRequest);

            // Надіслати запит
            transaction.sendRequest();

        } catch (Exception e) {
            e.printStackTrace(); // або логування
        }
    }
}
