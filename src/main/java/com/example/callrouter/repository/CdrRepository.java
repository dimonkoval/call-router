package com.example.callrouter.repository;

import com.example.callrouter.model.CdrRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdrRepository extends JpaRepository<CdrRecord, String> {}