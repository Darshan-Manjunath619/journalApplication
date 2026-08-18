package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.tag.*;
import com.darshan.journalApplication.tag.dto.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tags")
@SecurityRequirement(name = "bearerAuth")
public class TagsController {
    private final TagService tags;
    private final TagMapper mapper;

    public TagsController(TagService tags, TagMapper mapper) {
        this.tags = tags;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List owned tags")
    @ApiResponse(responseCode = "200", description = "Tags returned")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    public List<TagResponse> list(Authentication authentication) {
        return tags.listOwned(authentication.getName()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    @Operation(summary = "Create a tag", description = "Tag names are normalized and unique per user.")
    @ApiResponse(responseCode = "201", description = "Tag created")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "409", description = "Tag name already exists")
    public ResponseEntity<TagResponse> create(Authentication authentication,
                                               @Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(tags.create(authentication.getName(), request.name())));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rename an owned tag")
    @ApiResponse(responseCode = "200", description = "Tag renamed")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Owned tag not found")
    @ApiResponse(responseCode = "409", description = "Tag name already exists")
    public TagResponse rename(Authentication authentication, @PathVariable Long id,
                              @Valid @RequestBody TagRequest request) {
        return mapper.toResponse(tags.rename(id, authentication.getName(), request.name()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unused owned tag")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Owned tag not found")
    @ApiResponse(responseCode = "409", description = "Tag is assigned to a journal")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        tags.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
