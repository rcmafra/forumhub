package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.TopicCreateRequestDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateRequestDTO;
import com.raul.forumhub.topic.dto.response.HttpStatusMessage;
import com.raul.forumhub.topic.dto.response.TopicResponseDTO;
import com.raul.forumhub.topic.security.IsAuthenticated;
import com.raul.forumhub.topic.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forumhub.io/api/v1/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @IsAuthenticated
    @PostMapping("/create")
    public ResponseEntity<TopicResponseDTO> createTopic(@Valid @RequestBody TopicCreateRequestDTO topicCreateRequestDTO,
                                                         @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        TopicResponseDTO topicResponseDTO = this.topicService.createTopic(topicCreateRequestDTO, user_id);

        return ResponseEntity.status(HttpStatus.CREATED).body(topicResponseDTO);
    }

    @GetMapping("/listAll")
    public PagedModel<EntityModel<TopicResponseDTO>> topicsList(@PageableDefault Pageable pageable,
                                                                PagedResourcesAssembler<TopicResponseDTO> assembler) {

        return assembler.toModel(topicService.topicList(pageable));
    }

    @GetMapping("/searchTopicsByCourse")
    public PagedModel<EntityModel<TopicResponseDTO>> topicsListByCourse(Long course_id, @PageableDefault Pageable pageable,
                                                                        PagedResourcesAssembler<TopicResponseDTO> assembler) {
        return assembler.toModel(topicService.topicsListByCourse(course_id, pageable));
    }

    @GetMapping
    public ResponseEntity<TopicResponseDTO> getTopic(@RequestParam Long topic_id) {

        return ResponseEntity.ok(new TopicResponseDTO(topicService.getTopicById(topic_id)));
    }

    @PreAuthorize("hasAuthority('SCOPE_topic:edit')")
    @PutMapping("/{topic_id}/edit")
    public ResponseEntity<TopicResponseDTO> updateTopic(@PathVariable Long topic_id, @Valid @RequestBody TopicUpdateRequestDTO topicUpdateRequestDTO,
                                                        @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        TopicResponseDTO topicResponseDTO = this.topicService.updateTopic(topic_id, user_id, topicUpdateRequestDTO);

        return ResponseEntity.ok(topicResponseDTO);
    }

    @PreAuthorize("hasAuthority('SCOPE_topic:delete')")
    @DeleteMapping("/{topic_id}/delete")
    public ResponseEntity<HttpStatusMessage> deleteTopic(@PathVariable Long topic_id, @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        topicService.deleteTopic(topic_id, user_id);

        return ResponseEntity.ok(new HttpStatusMessage("HttpStatusCode OK"));
    }

}
