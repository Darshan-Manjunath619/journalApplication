package com.darshan.journalservice.tag.web;

import com.darshan.journalservice.identity.CurrentOwnerProvider;
import com.darshan.journalservice.shared.error.GlobalExceptionHandler;
import com.darshan.journalservice.tag.application.TagService;
import com.darshan.journalservice.tag.domain.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagsController.class)
@Import({TagMapper.class, GlobalExceptionHandler.class})
class TagsControllerTests {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    TagService tags;

    @MockitoBean
    CurrentOwnerProvider currentOwner;

    @MockitoBean
    JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void createsTagForCurrentOwner() throws Exception {
        Tag tag = new Tag();
        tag.setId(3L);
        tag.setName("Spring");
        when(currentOwner.requireOwnerId()).thenReturn(42L);
        when(tags.create(42L, "Spring")).thenReturn(tag);

        mvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Spring"));

        verify(tags).create(42L, "Spring");
    }

    @Test
    void rejectsBlankTagName() throws Exception {
        mvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
