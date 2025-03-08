package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseRequestDTO;
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


    public void createCourse(CourseRequestDTO courseRequestDTO) {
        Course course = Course.builder().name(courseRequestDTO.name())
                .category(courseRequestDTO.category())
                .build();

        this.courseRepository.save(course);
    }

    public List<CourseResponseCollection> getAllCourse(){
        return List.of(new CourseResponseCollection(this.courseRepository.findAll()));
    }

    public void deleteCourse(Long course_id){
        Course course = this.getCourseById(course_id);
        this.courseRepository.delete(course);
    }

    public CourseResponseDTO updateCourse(Long course_id, CourseRequestDTO courseUpdateDTO) {
        Course course = this.getCourseById(course_id);

        course.setName(courseUpdateDTO.name());
        course.setCategory(courseUpdateDTO.category());

        this.courseRepository.save(course);
        return new CourseResponseDTO(course);
    }

    public Course getCourseById(Long id) {
        return this.courseRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException("O curso informado não existe"));
    }


}
