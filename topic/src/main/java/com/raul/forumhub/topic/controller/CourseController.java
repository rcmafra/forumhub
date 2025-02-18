package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.CourseRequestDTO;
import com.raul.forumhub.topic.dto.response.CourseResponseCollection;
import com.raul.forumhub.topic.dto.response.CourseResponseDTO;
import com.raul.forumhub.topic.dto.response.HttpStatusMessage;
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
    public ResponseEntity<HttpStatusMessage> createCourse(@Valid @RequestBody CourseRequestDTO courseRequestDTO) {

        this.courseService.createCourse(courseRequestDTO);
        return new ResponseEntity<>(new HttpStatusMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @IsAuthenticated
    @GetMapping("/listAll")
    public ResponseEntity<List<CourseResponseCollection>> getAllCourse() {
        List<CourseResponseCollection> courseResponseCollection = this.courseService.getAllCourse();

        return new ResponseEntity<>(courseResponseCollection, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:edit')")
    @PutMapping("/edit/{courseName}")
    public ResponseEntity<CourseResponseDTO> updateCourse(@PathVariable String courseName,
                                                          @Valid @RequestBody CourseRequestDTO courseUpdateDTO) {
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        CourseResponseDTO courseResponseDTO = this.courseService.updateCourse(courseName, courseUpdateDTO);
        return ResponseEntity.ok(courseResponseDTO);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:delete')")
    @DeleteMapping("/delete/{courseName}")
    public ResponseEntity<HttpStatusMessage> deleteCourse(@PathVariable String courseName) {
        Assert.hasText(courseName, "O nome do curso não pode ser vazio");

        this.courseService.deleteCourse(courseName);
        return ResponseEntity.ok(new HttpStatusMessage("HttpStatusCode OK"));

    }


}
