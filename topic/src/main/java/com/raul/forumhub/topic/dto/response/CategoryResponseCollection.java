package com.raul.forumhub.topic.dto.response;

import com.raul.forumhub.topic.domain.Course;

import java.util.List;

public record CategoryResponseCollection(List<Course.Category> category) {
}
