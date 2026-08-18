package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.tag.dto.TagResponse;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt());
    }
}
