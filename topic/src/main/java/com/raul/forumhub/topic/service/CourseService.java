package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseCreateDTO;
import com.raul.forumhub.topic.dto.request.CourseUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetCourseCollection;
import com.raul.forumhub.topic.dto.response.GetCourseDTO;
import com.raul.forumhub.topic.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;


    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    public void createCourse(CourseCreateDTO courseCreateDTO){
        Course course = new Course(courseCreateDTO.name(), courseCreateDTO.category());
        this.courseRepository.save(course);
    }

    public List<GetCourseCollection> getAllCourse(){
        return List.of(new GetCourseCollection(this.courseRepository.findAll()));
    }

    public void deleteCourse(String courseName){
        Course course = this.getCourseByName(courseName);
        this.courseRepository.delete(course);
    }

    public GetCourseDTO updateNameCourse(String courseName, CourseUpdateDTO courseUpdateDTO){
        Course course1 = this.getCourseByName(courseName);
        course1.setName(courseUpdateDTO.name());

        this.courseRepository.save(course1);

        return new GetCourseDTO(course1);
    }

    private Course getCourseByName(String courseName){
        return this.courseRepository.findCourseByName(courseName).orElseThrow();
    }

    public Course getCourseById(Long id) throws Exception {
        return this.courseRepository.findById(id).orElseThrow(() -> new Exception("GetCourseDTO not found"));
    }


}
