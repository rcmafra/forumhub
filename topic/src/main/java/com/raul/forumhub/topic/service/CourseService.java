package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseDTO;
import com.raul.forumhub.topic.dto.response.CourseResponseCollection;
import com.raul.forumhub.topic.dto.response.CourseResponseDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;


    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    public void createCourse(CourseDTO courseDTO) {
        Course course = Course.builder().name(courseDTO.name())
                .category(courseDTO.category())
                .build();

        this.courseRepository.save(course);
    }

    public List<CourseResponseCollection> getAllCourse(){
        return List.of(new CourseResponseCollection(this.courseRepository.findAll()));
    }

    public void deleteCourse(String courseName){
        Course course = this.getCourseByName(courseName);
        this.courseRepository.delete(course);
    }

    public CourseResponseDTO updateCourse(String courseName, CourseDTO courseUpdateDTO) {
        Course course = this.getCourseByName(courseName);

        course.setName(courseUpdateDTO.name());
        course.setCategory(courseUpdateDTO.category());

        this.courseRepository.save(course);
        return new CourseResponseDTO(course);
    }

    private Course getCourseByName(String courseName){
        return this.courseRepository.findCourseByName(courseName).orElseThrow(() -> new InstanceNotFoundException("O curso informado não existe"));
    }

    public Course getCourseById(Long id) {
        return this.courseRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException("O curso informado não existe"));
    }


}
