package com.darshan.journalservice.tag.web;

import com.darshan.journalservice.tag.domain.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt());
    }
}
