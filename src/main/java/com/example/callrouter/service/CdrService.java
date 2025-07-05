package com.example.callrouter.service;

import com.example.callrouter.model.CdrRecord;
import com.example.callrouter.repository.CdrRepository;
import org.springframework.stereotype.Service;

@Service
public class CdrService {
    private final CdrRepository repo;
    public CdrService(CdrRepository repo) { this.repo = repo; }
    public CdrRecord onInvite(String callId, String from, String to) {
        CdrRecord cdr = new CdrRecord(callId, from, to);
        return repo.save(cdr);
    }
    public void onBye(String callId, long duration) {
        CdrRecord cdr = repo.findById(callId).orElseThrow();
        cdr.setDuration(duration);
        repo.save(cdr);
    }
}
