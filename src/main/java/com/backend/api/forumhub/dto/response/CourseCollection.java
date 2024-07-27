package com.backend.api.forumhub.dto.response;

import com.backend.api.forumhub.domain.Course;

import java.util.List;

public record CourseCollection(List<Course> course) {
}
