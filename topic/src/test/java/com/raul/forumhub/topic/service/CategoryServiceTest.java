package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    CategoryService categoryService;

    @Test
    void shouldReturnAllCategoriessCreatedWithSuccessful() {
        List<Course.Category> category =
                Assertions.assertDoesNotThrow(() -> this.categoryService.getAllCategories());

        assertThat(category).isNotEmpty();

    }


}
