package com.raul.forumhub.topic.repository;

import com.raul.forumhub.topic.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findCourseByName(String courseName);

}
