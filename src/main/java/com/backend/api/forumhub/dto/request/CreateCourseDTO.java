package com.backend.api.forumhub.dto.request;

import com.backend.api.forumhub.domain.Course;

public record CreateCourseDTO(String name, Course.Category category) {
}
