package com.raul.forumhub.topic.dto.request;

import com.raul.forumhub.topic.domain.Topic;

public record TopicUpdateDTO(Topic topic, Long course_id) {

}
