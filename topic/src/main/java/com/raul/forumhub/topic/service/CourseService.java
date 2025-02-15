package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseCreateDTO;
import com.raul.forumhub.topic.dto.request.CourseUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetCourseCollection;
import com.raul.forumhub.topic.dto.response.GetCourseDTO;
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


    public void createCourse(CourseCreateDTO courseCreateDTO) {
        Course course = Course.builder().name(courseCreateDTO.name())
                .category(courseCreateDTO.category())
                .build();

        this.courseRepository.save(course);
    }

    public List<GetCourseCollection> getAllCourse(){
        return List.of(new GetCourseCollection(this.courseRepository.findAll()));
    }

    public void deleteCourse(String courseName){
        Course course = this.getCourseByName(courseName);
        this.courseRepository.delete(course);
    }

    public GetCourseDTO updateCourse(String courseName, CourseUpdateDTO courseUpdateDTO) {
        Course course = this.getCourseByName(courseName);

        course.setName(courseUpdateDTO.name());
        course.setCategory(courseUpdateDTO.category());

        this.courseRepository.save(course);
        return new GetCourseDTO(course);
    }

    private Course getCourseByName(String courseName){
        return this.courseRepository.findCourseByName(courseName).orElseThrow(() -> new InstanceNotFoundException("O curso informado não existe"));
    }

    public Course getCourseById(Long id) {
        return this.courseRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException("O curso informado não existe"));
    }


}
