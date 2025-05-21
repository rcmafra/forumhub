package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.topic.security.TopicSecurityConfig;
import com.raul.forumhub.topic.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {CategoryController.class,
        TopicSecurityConfig.class, GlobalExceptionHandler.class})
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CategoryService categoryService;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @DisplayName("Should fail with status code 401 when return all categories if unauthenticated")
    @Test
    void shouldFailToReturnAllCategoriesIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.categoryService);

    }

    @DisplayName("Should fail with status code 403 when return all categories if user hasn't suitable authority")
    @Test
    void shouldFailToReturnAllCategoriesIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.categoryService);

    }

    @DisplayName("Should return all categories with successful if has suitable authorities")
    @Test
    void shouldReturnAllCategoriesWithSuccessfulIfHasSuitableAuthories() throws Exception {
        BDDMockito.given(this.categoryService.getAllCategories())
                .willReturn(List.of(Course.Category.values()));

        this.mockMvc.perform(get("/forumhub.io/api/v1/categories/listAll")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_category:readAll"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"category\":[\"JAVA\",\"C\",\"CPLUSPLUS\",\"CSHARP\"," +
                                          "\"GOLANG\",\"QA\",\"CLOUD_COMPUTATION\",\"DEVOPS\"]}"));


        BDDMockito.verify(this.categoryService).getAllCategories();
        BDDMockito.verifyNoMoreInteractions(this.categoryService);

    }
}
