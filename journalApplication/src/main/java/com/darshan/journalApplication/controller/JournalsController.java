package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.JournalMapper;
import com.darshan.journalApplication.journal.dto.*;
import com.darshan.journalApplication.service.JournalEntryService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.darshan.journalApplication.journal.JournalSearchCriteria;
import com.darshan.journalApplication.shared.error.InvalidQueryParameterException;
import com.darshan.journalApplication.shared.web.PageResponse;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/journals")
@Validated
@Tag(name = "Journals")
@SecurityRequirement(name = "bearerAuth")
public class JournalsController {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "title");
    private final JournalEntryService journals;
    private final JournalMapper mapper;

    public JournalsController(JournalEntryService journals, JournalMapper mapper) {
        this.journals = journals;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Search owned journals", description = "Returns a bounded, stable page of journals owned by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Page returned")
    @ApiResponse(responseCode = "400", description = "Pagination, date, tag, or sort parameter is invalid")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    public PageResponse<JournalResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false, name = "tag") @Min(1) Long tagId,
            @Parameter(example = "createdAt,desc", description = "Allowed fields: createdAt, updatedAt, title; direction: asc or desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        SortSelection selection = parseSort(sort);
        return PageResponse.from(journals.searchOwned(authentication.getName(),
                new JournalSearchCriteria(query, from, to, favorite, tagId), page, size,
                selection.field(), selection.direction()), mapper::toResponse);
    }

    @PostMapping
    @Operation(summary = "Create a journal")
    @ApiResponse(responseCode = "201", description = "Journal created")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "A referenced owned tag was not found")
    public ResponseEntity<JournalResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateJournalRequest request) {
        JournalEntry saved = journals.createOwned(
                mapper.toEntity(request), authentication.getName(), request.tagIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an owned journal")
    @ApiResponse(responseCode = "200", description = "Journal returned")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Owned journal not found")
    public JournalResponse get(Authentication authentication, @PathVariable Long id) {
        return mapper.toResponse(journals.getOwned(id, authentication.getName()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an owned journal", description = "Updates only supplied fields, including favorite and tag assignments.")
    @ApiResponse(responseCode = "200", description = "Journal updated")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Owned journal or referenced tag not found")
    public JournalResponse update(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody UpdateJournalRequest request) {
        return mapper.toResponse(journals.updateOwned(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an owned journal")
    @ApiResponse(responseCode = "204", description = "Journal deleted")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Owned journal not found")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        journals.deleteOwned(id, authentication.getName());
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

    private record SortSelection(String field, Sort.Direction direction) {}
}
