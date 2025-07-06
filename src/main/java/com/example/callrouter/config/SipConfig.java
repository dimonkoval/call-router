package com.example.callrouter.config;

import com.example.callrouter.service.SipListenerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import java.util.Properties;

@Configuration
public class SipConfig {

    @Value("${sip.ip}")
    private String ip;

    @Value("${sip.port}")
    private int port;

    // 1) SipFactory singleton
    @Bean
    public SipFactory sipFactory() {
        return SipFactory.getInstance();
    }

    // 2) Властивості стека, включно з логами
    @Bean
    public Properties sipProperties() {
        Properties props = new Properties();
        props.setProperty("javax.sip.STACK_NAME", "call-router-stack");
        props.setProperty("javax.sip.IP_ADDRESS", ip);
        props.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

        // Шляхи для логування NIST SIP stack
        props.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/sip_debug.log");
        props.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/sip_server.log");
        props.setProperty("gov.nist.javax.sip.ROUTER_PATH", "logs");
        return props;
    }

    // 3) Створюємо сам SipStack
    @Bean
    public SipStack sipStack(SipFactory sipFactory, Properties sipProperties)
            throws PeerUnavailableException {
        return sipFactory.createSipStack(sipProperties);
    }

    // 4) ListeningPoint (IP+порт+транспорт)
    @Bean
    public ListeningPoint listeningPoint(SipStack sipStack) throws Exception {
        return sipStack.createListeningPoint(ip, port, ListeningPoint.UDP);
    }

    // 5) SipProvider – єдиний, «правильний» та єдиний бекон для всіх залежностей
    @Bean
    public SipProvider sipProvider(
            SipStack sipStack,
            ListeningPoint listeningPoint,
            @Lazy SipListenerImpl sipListener) throws Exception {
        SipProvider provider = sipStack.createSipProvider(listeningPoint);
        provider.addSipListener(sipListener);
        return provider;
    }

    // 6) Решта фабрик для MessageDispatcher
    @Bean
    public MessageFactory messageFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createMessageFactory();
    }

    @Bean
    public AddressFactory addressFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createAddressFactory();
    }

    @Bean
    public HeaderFactory headerFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createHeaderFactory();
    }
}
