package com.raul.forumhub.topic.dto.request;

import com.raul.forumhub.topic.domain.Status;

public record TopicUpdateDTO(String title, String question, Status status, Long course_id) {

}
