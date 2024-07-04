package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Course;
import com.backend.api.forumhub.dto.request.CreateCourseDTO;
import com.backend.api.forumhub.repository.CourseRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public void createCourse(CreateCourseDTO createCourseDTO){
        Course course = new Course(createCourseDTO.name(), createCourseDTO.category());
        this.courseRepository.save(course);
    }

    public Course getCourseById(Long id) throws Exception {
        return this.courseRepository.findById(id).orElseThrow(() -> new Exception("Course not found"));
    }

    public Course getCourseByName(String name) throws Exception {
        return this.courseRepository.findByName(name).orElseThrow(() -> new Exception("Course not found"));
    }


}
