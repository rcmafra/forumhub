package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Course;
import com.backend.api.forumhub.dto.request.CreateCourseDTO;
import com.backend.api.forumhub.dto.request.UpdateCourse;
import com.backend.api.forumhub.dto.response.CourseCollection;
import com.backend.api.forumhub.dto.response.CourseDTO;
import com.backend.api.forumhub.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<CourseCollection> getAllCourse(){
        return List.of(new CourseCollection(this.courseRepository.findAll()));
    }

    public void deleteCourse(String courseName){
        Course course = this.getCourseByName(courseName);
        this.courseRepository.delete(course);
    }

    private Course getCourseByName(String courseName){
        return this.courseRepository.findCourseByName(courseName).orElseThrow();
    }

    public Course getCourseById(Long id) throws Exception {
        return this.courseRepository.findById(id).orElseThrow(() -> new Exception("CourseDTO not found"));
    }


}
