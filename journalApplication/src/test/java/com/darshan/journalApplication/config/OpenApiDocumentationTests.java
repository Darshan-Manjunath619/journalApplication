package com.darshan.journalApplication.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void versionedContractPublishesSecurityAndHidesLegacyRoutes() throws Exception {
        mockMvc.perform(get("/v3/api-docs/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Journal Application API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security")
                        .doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/journals'].get.security[0].bearerAuth")
                        .isArray())
                .andExpect(jsonPath("$.paths['/api/v1/journals'].get.responses['400']")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.RegisterRequest.properties.userName.example")
                        .value("darshan_01"))
                .andExpect(jsonPath("$.components.schemas.CreateJournalRequest.properties.title.maxLength")
                        .value(160))
                .andExpect(jsonPath("$.paths['/journal']").doesNotExist())
                .andExpect(jsonPath("$.paths['/public/login']").doesNotExist());
    }
}
