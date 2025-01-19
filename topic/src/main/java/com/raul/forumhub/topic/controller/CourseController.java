package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.CourseCreateDTO;
import com.raul.forumhub.topic.dto.request.CourseUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetCourseCollection;
import com.raul.forumhub.topic.dto.response.GetCourseDTO;
import com.raul.forumhub.topic.dto.response.HttpMessageDefault;
import com.raul.forumhub.topic.security.IsAuthenticated;
import com.raul.forumhub.topic.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
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
    public ResponseEntity<HttpMessageDefault> createCourse(@Valid @RequestBody CourseCreateDTO courseCreateDTO){

        this.courseService.createCourse(courseCreateDTO);
        return new ResponseEntity<>(new HttpMessageDefault("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @IsAuthenticated
    @GetMapping("/listAll")
    public ResponseEntity<List<GetCourseCollection>> getAllCourse(){
        List<GetCourseCollection> getCourseCollection = this.courseService.getAllCourse();

        return new ResponseEntity<>(getCourseCollection, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:edit')")
    @PutMapping("/edit")
    public  ResponseEntity<GetCourseDTO> updateNameCourse(@RequestParam String courseName,
                                                          @Valid @RequestBody CourseUpdateDTO courseUpdateDTO){
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        GetCourseDTO getCourseDTO = this.courseService.updateNameCourse(courseName, courseUpdateDTO);
        return ResponseEntity.ok(getCourseDTO);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpMessageDefault> deleteCourse(@RequestParam String courseName){
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        this.courseService.deleteCourse(courseName);
        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));

    }


}
