package com.example.callrouter.controller;

import com.example.callrouter.dto.CallDetailRecordDTO;
import com.example.callrouter.mapper.CallDetailRecordMapper;
import com.example.callrouter.model.CallDetailRecord;
import com.example.callrouter.repository.CdrRepository;
import com.example.callrouter.service.CdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cdr")
public class CdrController {
    private final CdrRepository repo;
    private final CallDetailRecordMapper mapper;
    private final CdrService cdrService;

    @GetMapping
    public String listCdr(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "setupTime") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(required = false) String search,
            Model model
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<CallDetailRecordDTO> cdrPage = cdrService.getCallRecords(search, pageable);

        model.addAttribute("cdrPage", cdrPage);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("search", search);

        return "cdr/list";
    }
}