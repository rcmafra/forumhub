package com.raul.forumhub.topic.dto.request;

import com.raul.forumhub.topic.domain.Course;

public record CourseCreateDTO(String name, Course.Category category) {
}
