package com.darshan.journalservice.journal.web;

import com.darshan.journalservice.identity.CurrentOwnerProvider;
import com.darshan.journalservice.journal.application.JournalSearchCriteria;
import com.darshan.journalservice.journal.application.JournalService;
import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.shared.error.InvalidQueryParameterException;
import com.darshan.journalservice.shared.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/journals")
@Validated
public class JournalsController {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "title");

    private final JournalService journals;
    private final JournalMapper mapper;
    private final CurrentOwnerProvider currentOwner;

    public JournalsController(JournalService journals, JournalMapper mapper,
                              CurrentOwnerProvider currentOwner) {
        this.journals = journals;
        this.mapper = mapper;
        this.currentOwner = currentOwner;
    }

    @GetMapping
    public PageResponse<JournalResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false, name = "tag") @Min(1) Long tagId,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        SortSelection selection = parseSort(sort);
        return PageResponse.from(journals.searchOwned(currentOwner.requireOwnerId(),
                new JournalSearchCriteria(query, from, to, favorite, tagId), page, size,
                selection.field(), selection.direction()), mapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<JournalResponse> create(@Valid @RequestBody CreateJournalRequest request) {
        JournalEntry saved = journals.create(mapper.toEntity(request),
                currentOwner.requireOwnerId(), request.tagIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public JournalResponse get(@PathVariable Long id) {
        return mapper.toResponse(journals.getOwned(id, currentOwner.requireOwnerId()));
    }

    @PatchMapping("/{id}")
    public JournalResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateJournalRequest request) {
        return mapper.toResponse(journals.update(id, currentOwner.requireOwnerId(),
                mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        journals.delete(id, currentOwner.requireOwnerId());
        return ResponseEntity.noContent().build();
    }

    private SortSelection parseSort(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length != 2 || !ALLOWED_SORT_FIELDS.contains(parts[0])) {
            throw new InvalidQueryParameterException(
                    "sort must use createdAt, updatedAt, or title");
        }
        try {
            return new SortSelection(parts[0], Sort.Direction.fromString(parts[1]));
        } catch (IllegalArgumentException exception) {
            throw new InvalidQueryParameterException("sort direction must be asc or desc");
        }
    }

    private record SortSelection(String field, Sort.Direction direction) {
    }
}
