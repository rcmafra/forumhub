package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.CourseDTO;
import com.raul.forumhub.topic.dto.response.CourseResponseCollection;
import com.raul.forumhub.topic.dto.response.CourseResponseDTO;
import com.raul.forumhub.topic.dto.response.HttpMessage;
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
@RequestMapping("/forumhub.io/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:create')")
    @PostMapping("/create")
    public ResponseEntity<HttpMessage> createCourse(@Valid @RequestBody CourseDTO courseDTO) {

        this.courseService.createCourse(courseDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @IsAuthenticated
    @GetMapping("/listAll")
    public ResponseEntity<List<CourseResponseCollection>> getAllCourse() {
        List<CourseResponseCollection> courseResponseCollection = this.courseService.getAllCourse();

        return new ResponseEntity<>(courseResponseCollection, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:edit')")
    @PutMapping("/edit")
    public ResponseEntity<CourseResponseDTO> updateCourse(@RequestParam String courseName,
                                                          @Valid @RequestBody CourseDTO courseUpdateDTO) {
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        CourseResponseDTO courseResponseDTO = this.courseService.updateCourse(courseName, courseUpdateDTO);
        return ResponseEntity.ok(courseResponseDTO);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpMessage> deleteCourse(@RequestParam String courseName) {
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        this.courseService.deleteCourse(courseName);
        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));

    }


}
