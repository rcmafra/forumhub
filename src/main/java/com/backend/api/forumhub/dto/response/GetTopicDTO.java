package com.backend.api.forumhub.dto.response;

import com.backend.api.forumhub.domain.Answer;
import com.backend.api.forumhub.domain.Course;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.domain.Status;

import java.time.LocalDateTime;
import java.util.List;

public record GetTopicDTO(Long id, String title, String message, LocalDateTime createdAt, Status status, User author, Course course, List<Answer> answer) {

    public GetTopicDTO(Topic topic){
        this(topic.getId(), topic.getTitle(), topic.getMessage(), topic.getCreatedAt(), topic.getStatus(), topic.getAuthor(), topic.getCourse(), topic.getAnswer());
    }
}
