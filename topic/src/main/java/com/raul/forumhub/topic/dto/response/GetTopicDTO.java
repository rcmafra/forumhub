package com.raul.forumhub.topic.dto.response;

import com.raul.forumhub.topic.domain.*;

import java.time.LocalDateTime;
import java.util.Set;

public record GetTopicDTO(Long id, String title, String question, LocalDateTime createdAt, Status status, Author author,
                          Course course, Set<Answer> answer) {

    public GetTopicDTO(Topic topic) {
        this(topic.getId(), topic.getTitle(), topic.getQuestion(), topic.getCreatedAt(), topic.getStatus(),
                topic.getAuthor(), topic.getCourse(), topic.getAnswers());
    }
}
