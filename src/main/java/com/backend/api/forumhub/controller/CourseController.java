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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-forum/v1/forumhub/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService){
        this.courseService = courseService;
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:create')")
    @PostMapping("/create")
    public ResponseEntity<HttpMessage> createCourse(@Valid @RequestBody CreateCourseDTO createCourseDTO){

        this.courseService.createCourse(createCourseDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @PreAuthorize("authenticated")
    @GetMapping
    public ResponseEntity<List<CourseCollection>> getAllCourse(){
        List<CourseCollection> courseCollection = this.courseService.getAllCourse();

        return new ResponseEntity<>(courseCollection, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:delete')")
    @DeleteMapping("/delete/{courseName}")
    public ResponseEntity<HttpMessage> deleteCourse(@PathVariable String courseName){
        this.courseService.deleteCourse(courseName);

        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));

    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:edit')")
    @PutMapping("/{courseName}")
    public  ResponseEntity<CourseDTO> updateNameCourse(@PathVariable String courseName, @RequestBody UpdateCourse updateCourse){
        CourseDTO courseDTO = this.courseService.updateNameCourse(courseName, updateCourse);

        return ResponseEntity.ok(courseDTO);
    }


}
