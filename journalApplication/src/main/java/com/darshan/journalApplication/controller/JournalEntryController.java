package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.JournalMapper;
import com.darshan.journalApplication.journal.dto.*;
import com.darshan.journalApplication.service.JournalEntryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/journal")
@Tag(name = "Journal Entries")
public class JournalEntryController {
    private final JournalEntryService service;
    private final JournalMapper mapper;

    public JournalEntryController(JournalEntryService service, JournalMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<JournalResponse> getAll(Authentication authentication) {
        return service.getAllByOwner(authentication.getName()).stream()
                .map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<JournalResponse> create(
            @Valid @RequestBody CreateJournalRequest request,
            Authentication authentication) {
        JournalEntry saved = service.EntryRecord(
                mapper.toEntity(request), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @GetMapping("/id/{id}")
    public JournalResponse getById(@PathVariable Long id, Authentication authentication) {
        return mapper.toResponse(service.getOwned(id, authentication.getName()));
    }

    @PatchMapping("/{id}")
    public JournalResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateJournalRequest request,
                                  Authentication authentication) {
        return mapper.toResponse(service.updateOwned(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.deleteById(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
