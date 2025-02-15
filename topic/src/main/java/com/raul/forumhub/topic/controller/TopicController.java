package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
import com.raul.forumhub.topic.dto.response.TopicResponseDTO;
import com.raul.forumhub.topic.dto.response.HttpMessage;
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

    public TopicController(TopicService topicService){
        this.topicService = topicService;
    }

    @IsAuthenticated
    @PostMapping("/create")
    public ResponseEntity<HttpMessage> createTopic(@Valid @RequestBody TopicCreateDTO topicCreateDTO,
                                                   @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.topicService.createTopic(topicCreateDTO, user_id);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/listAll")
    public PagedModel<EntityModel<TopicResponseDTO>> topicsList(@PageableDefault Pageable pageable,
                                                                PagedResourcesAssembler<TopicResponseDTO> assembler) {

        return assembler.toModel(topicService.topicList(pageable));
    }

    @GetMapping
    public ResponseEntity<TopicResponseDTO> getTopic(@RequestParam Long topic_id){

        return ResponseEntity.ok(new TopicResponseDTO(topicService.getTopicById(topic_id)));
    }

    @PreAuthorize("hasAuthority('SCOPE_topic:edit')")
    @PutMapping("/edit")
    public ResponseEntity<TopicResponseDTO> updateTopic(@RequestParam Long topic_id, @Valid @RequestBody TopicUpdateDTO topicUpdateDTO,
                                                        @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        TopicResponseDTO topicResponseDTO = this.topicService.updateTopic(topic_id, user_id, topicUpdateDTO);

        return ResponseEntity.ok(topicResponseDTO);
    }

    @PreAuthorize("hasAuthority('SCOPE_topic:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpMessage> deleteTopic(@RequestParam Long topic_id, @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        topicService.deleteTopic(topic_id, user_id);

        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));
    }

}
