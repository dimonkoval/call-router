//package com.example.callrouter.sip;
//
//import javax.sip.RequestEvent;
//import javax.sip.ResponseEvent;
//import javax.sip.header.ToHeader;
//import javax.sip.address.SipURI;
//import javax.sip.message.Request;
//import javax.sip.message.Response;
//
//public class SipInviteHandler {
//    public void handle(RequestEvent evt) {
//        Request req = evt.getRequest();
//        String target = ((SipURI)((ToHeader)req.getHeader(ToHeader.NAME)).getAddress().getURI()).getUser();
//    }
//}
