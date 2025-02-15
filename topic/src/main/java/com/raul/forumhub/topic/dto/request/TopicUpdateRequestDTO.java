package com.raul.forumhub.topic.dto.request;

import com.raul.forumhub.topic.domain.Status;
import jakarta.validation.constraints.NotBlank;

public record TopicUpdateRequestDTO(@NotBlank(message = "O título não pode ser vazio") String title,
                                    @NotBlank(message = "A pergunta não pode ser vazia") String question,
                                    Status status,
                                    Long course_id) {

}
