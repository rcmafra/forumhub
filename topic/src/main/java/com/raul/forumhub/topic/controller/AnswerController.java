package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.AnswerTopicDTO;
import com.raul.forumhub.topic.dto.request.AnswerUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetAnswerDTO;
import com.raul.forumhub.topic.dto.response.HttpMessageDefault;
import com.raul.forumhub.topic.security.IsAuthenticated;
import com.raul.forumhub.topic.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-forum/v1/forumhub/topics")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService){
        this.answerService = answerService;
    }

    @IsAuthenticated
    @PostMapping("/{topic_id}/answer")
    public ResponseEntity<HttpMessageDefault> answerTopic(@PathVariable Long topic_id, @Valid @RequestBody AnswerTopicDTO answerTopicDTO,
                                                          @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.answerTopic(topic_id, user_id, answerTopicDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @IsAuthenticated
    @PostMapping("/{topic_id}/markBestAnswer")
    public ResponseEntity<HttpMessageDefault> markBestAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                         @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.markBestAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }

    @PreAuthorize("hasAuthority('SCOPE_answer:edit')")
    @PutMapping("/{topic_id}/answers/edit")
    public ResponseEntity<GetAnswerDTO> updateAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                     @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AnswerUpdateDTO answerUpdateDTO){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        GetAnswerDTO getAnswerDTO = this.answerService.updateAnswer(topic_id, answer_id, user_id, answerUpdateDTO);

        return ResponseEntity.ok(getAnswerDTO);
    }

    @PreAuthorize("hasAuthority('SCOPE_answer:delete')")
    @DeleteMapping("/{topic_id}/answers/delete")
    public ResponseEntity<HttpMessageDefault> deleteAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                           @AuthenticationPrincipal Jwt jwt){

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.deleteAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }

}
