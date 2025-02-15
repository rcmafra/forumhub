package com.raul.forumhub.topic.dto.request;

import com.raul.forumhub.topic.domain.Course;
import jakarta.validation.constraints.NotBlank;

public record CourseDTO(@NotBlank(message = "O nome do curso não pode ser vazio") String name,
                        Course.Category category) {
}
