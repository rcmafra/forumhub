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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forumhub.io/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:write')")
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

    @IsAuthenticated
    @GetMapping
    public ResponseEntity<CourseResponseDTO> getCourse(@RequestParam Long course_id) {
        return ResponseEntity.ok(new CourseResponseDTO(this.courseService.getCourseById(course_id)));
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:edit')")
    @PutMapping("/{course_id}/edit")
    public ResponseEntity<CourseResponseDTO> updateCourse(@PathVariable Long course_id,
                                                          @Valid @RequestBody CourseRequestDTO courseUpdateDTO) {

        CourseResponseDTO courseResponseDTO = this.courseService.updateCourse(course_id, courseUpdateDTO);
        return ResponseEntity.ok(courseResponseDTO);
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_course:delete')")
    @DeleteMapping("/{course_id}/delete")
    public ResponseEntity<HttpStatusMessage> deleteCourse(@PathVariable Long course_id) {

        this.courseService.deleteCourse(course_id);
        return ResponseEntity.ok(new HttpStatusMessage("HttpStatusCode OK"));

    }


}
