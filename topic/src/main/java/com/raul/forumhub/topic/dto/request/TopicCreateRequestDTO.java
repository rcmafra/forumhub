package com.raul.forumhub.topic.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TopicCreateRequestDTO(@NotBlank(message = "O título não pode ser vazio") String title,
                                    @NotBlank(message = "A pergunta não pode ser vazia") String question,
                                    Long course_id) {
}
