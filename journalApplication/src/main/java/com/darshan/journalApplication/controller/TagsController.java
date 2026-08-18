package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.tag.*;
import com.darshan.journalApplication.tag.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagsController {
    private final TagService tags;
    private final TagMapper mapper;

    public TagsController(TagService tags, TagMapper mapper) {
        this.tags = tags;
        this.mapper = mapper;
    }

    @GetMapping
    public List<TagResponse> list(Authentication authentication) {
        return tags.listOwned(authentication.getName()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(Authentication authentication,
                                               @Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(tags.create(authentication.getName(), request.name())));
    }

    @PatchMapping("/{id}")
    public TagResponse rename(Authentication authentication, @PathVariable Long id,
                              @Valid @RequestBody TagRequest request) {
        return mapper.toResponse(tags.rename(id, authentication.getName(), request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        tags.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
