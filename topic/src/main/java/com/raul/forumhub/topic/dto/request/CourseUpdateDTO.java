package com.raul.forumhub.topic.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CourseUpdateDTO(@NotBlank(message = "O nome do curso não pode ser vazio") String name) {
}
