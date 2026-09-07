package com.darshan.journalservice.tag.web;

import com.darshan.journalservice.identity.CurrentOwnerProvider;
import com.darshan.journalservice.tag.application.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagsController {
    private final TagService tags;
    private final TagMapper mapper;
    private final CurrentOwnerProvider currentOwner;

    public TagsController(TagService tags, TagMapper mapper,
                          CurrentOwnerProvider currentOwner) {
        this.tags = tags;
        this.mapper = mapper;
        this.currentOwner = currentOwner;
    }

    @GetMapping
    public List<TagResponse> list() {
        return tags.listOwned(currentOwner.requireOwnerId()).stream()
                .map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(tags.create(currentOwner.requireOwnerId(), request.name())));
    }

    @PatchMapping("/{id}")
    public TagResponse rename(@PathVariable Long id,
                              @Valid @RequestBody TagRequest request) {
        return mapper.toResponse(tags.rename(id, currentOwner.requireOwnerId(), request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tags.delete(id, currentOwner.requireOwnerId());
        return ResponseEntity.noContent().build();
    }
}
