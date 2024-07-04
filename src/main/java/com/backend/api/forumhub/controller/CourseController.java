package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.dto.request.CreateCourseDTO;
import com.backend.api.forumhub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-forum/v1/forumhub/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService){
        this.courseService = courseService;
    }

    @PostMapping("/create/")
    private ResponseEntity<HttpMessage> createCourse(@Valid @RequestBody CreateCourseDTO createCourseDTO){
        this.courseService.createCourse(createCourseDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }
}
