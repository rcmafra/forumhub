package com.raul.forumhub.topic.integration.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles(value = "test")
class CategoryControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @DisplayName("Should fail with status code 401 when return all categories if unauthenticated")
    @Test
    void shouldFailToReturnAllCategoriesIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

    }

    @DisplayName("Should fail with status code 403 when return all categories if user hasn't suitable authority")
    @Test
    void shouldFailToReturnAllCategoriesIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

    }

    @DisplayName("Should return all coucategories with successful if has suitable authorities")
    @Test
    void shouldReturnAllCategoriesWithSuccessfulIfHasSuitableAuthories() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_category:readAll"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"category\":[\"JAVA\",\"C\",\"CPLUSPLUS\",\"CSHARP\"," +
                                          "\"GOLANG\",\"QA\",\"CLOUD_COMPUTATION\",\"DevOps\"]}"));

    }
}
