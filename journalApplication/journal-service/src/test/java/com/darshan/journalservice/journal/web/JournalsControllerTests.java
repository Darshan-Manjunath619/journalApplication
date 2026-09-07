package com.darshan.journalservice.journal.web;

import com.darshan.journalservice.identity.AuthenticationRequiredException;
import com.darshan.journalservice.identity.CurrentOwnerProvider;
import com.darshan.journalservice.identity.JwtTokenVerifier;
import com.darshan.journalservice.journal.application.JournalSearchCriteria;
import com.darshan.journalservice.journal.application.JournalService;
import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.shared.error.GlobalExceptionHandler;
import com.darshan.journalservice.tag.web.TagMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = JournalsController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({JournalMapper.class, TagMapper.class, GlobalExceptionHandler.class})
class JournalsControllerTests {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    JournalService journals;

    @MockitoBean
    CurrentOwnerProvider currentOwner;

    @MockitoBean
    JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    JwtTokenVerifier jwtTokenVerifier;

    @Test
    void listsOnlyForCurrentOwnerUsingStablePageContract() throws Exception {
        JournalEntry entry = new JournalEntry();
        entry.setId(7L);
        entry.setTitle("Microservices");
        when(currentOwner.requireOwnerId()).thenReturn(42L);
        when(journals.searchOwned(eq(42L), any(JournalSearchCriteria.class),
                eq(0), eq(20), eq("createdAt"), eq(Sort.Direction.DESC)))
                .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/journals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].title").value("Microservices"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsInvalidCreateRequestBeforeCallingService() throws Exception {
        mvc.perform(post("/api/v1/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void rejectsInvalidSortAllowlist() throws Exception {
        mvc.perform(get("/api/v1/journals").param("sort", "ownerId,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid query parameter"));
    }

    @Test
    void remainsLockedUntilJwtOwnerProviderIsImplemented() throws Exception {
        when(currentOwner.requireOwnerId()).thenThrow(new AuthenticationRequiredException());

        mvc.perform(get("/api/v1/journals/7"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"));

        verify(currentOwner).requireOwnerId();
    }
}
