package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.JournalMapper;
import com.darshan.journalApplication.journal.dto.*;
import com.darshan.journalApplication.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journals")
public class JournalsController {
    private final JournalEntryService journals;
    private final JournalMapper mapper;

    public JournalsController(JournalEntryService journals, JournalMapper mapper) {
        this.journals = journals;
        this.mapper = mapper;
    }

    @GetMapping
    public List<JournalResponse> list(Authentication authentication) {
        return journals.getAllByOwner(authentication.getName()).stream()
                .map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<JournalResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateJournalRequest request) {
        JournalEntry saved = journals.createOwned(
                mapper.toEntity(request), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public JournalResponse get(Authentication authentication, @PathVariable Long id) {
        return mapper.toResponse(journals.getOwned(id, authentication.getName()));
    }

    @PatchMapping("/{id}")
    public JournalResponse update(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody UpdateJournalRequest request) {
        return mapper.toResponse(journals.updateOwned(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        journals.deleteOwned(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
