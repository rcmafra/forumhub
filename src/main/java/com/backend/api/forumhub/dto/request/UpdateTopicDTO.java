package com.backend.api.forumhub.dto.request;

import com.backend.api.forumhub.domain.Topic;

public record UpdateTopicDTO(Topic topic, Long course_id) {

}
