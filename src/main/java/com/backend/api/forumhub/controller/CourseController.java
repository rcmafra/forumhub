package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.request.CreateCourseDTO;
import com.backend.api.forumhub.dto.request.UpdateCourse;
import com.backend.api.forumhub.dto.response.CourseCollection;
import com.backend.api.forumhub.dto.response.CourseDTO;
import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/")
    private ResponseEntity<List<CourseCollection>> getAllCourse(){
        List<CourseCollection> courseCollection = this.courseService.getAllCourse();
        return new ResponseEntity<>(courseCollection, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{courseName}")
    private ResponseEntity<HttpMessage> deleteCourse(@PathVariable String courseName){
        this.courseService.deleteCourse(courseName);
        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));

    }

    @PutMapping("/{courseName}")
    private ResponseEntity<CourseDTO> updateNameCourse(@PathVariable String courseName, @RequestBody UpdateCourse updateCourse){
        CourseDTO courseDTO = this.courseService.updateNameCourse(courseName, updateCourse);
        return ResponseEntity.ok(courseDTO);
    }


}
