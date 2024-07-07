package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.request.CreateTopicDTO;
import com.backend.api.forumhub.dto.request.UpdateTopicDTO;
import com.backend.api.forumhub.dto.response.GetTopicDTO;
import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-forum/v1/forumhub/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService){
        this.topicService = topicService;
    }

    @PostMapping("/create/")
    private ResponseEntity<HttpMessage> createTopic(@Valid @RequestBody CreateTopicDTO createTopicDTO,
                                                    @AuthenticationPrincipal Jwt jwt) throws Exception {

        Assert.notNull(jwt,"Bearer token can't be empty");
        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.topicService.createTopic(createTopicDTO, user_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @GetMapping("/")
    private PagedModel<EntityModel<GetTopicDTO>> topicsList(@PageableDefault Pageable pageable,
                                                            PagedResourcesAssembler<GetTopicDTO> assembler) {

        Page<GetTopicDTO> topicsListDTOS = topicService.topicList(pageable);
        return assembler.toModel(topicsListDTOS);
    }

    @GetMapping("/{topic_id}/")
    private ResponseEntity<GetTopicDTO> getTopic(@PathVariable Long topic_id) throws Exception {

        return ResponseEntity.ok(new
                GetTopicDTO(topicService.getTopicById(topic_id)));
    }


    @PutMapping("/{topic_id}/")
    private ResponseEntity<HttpMessage> updateTopic(@PathVariable Long topic_id,  @Valid @RequestBody UpdateTopicDTO updateTopicDTO,
                                                    @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.topicService.updateTopic(topic_id, user_id, updateTopicDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{topic_id}/")
    private ResponseEntity<HttpMessage> deleteTopic(@PathVariable Long topic_id, @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        topicService.deleteTopic(topic_id, user_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

}
