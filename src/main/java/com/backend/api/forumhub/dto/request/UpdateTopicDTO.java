package com.backend.api.forumhub.dto.request;

import com.backend.api.forumhub.domain.Topic;

public record UpdateTopicDTO(Long author_id, Topic topic, Long course_id) {

}
