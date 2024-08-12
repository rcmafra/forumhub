package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.AnswerTopicDTO;
import com.raul.forumhub.topic.dto.request.AnswerUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetAnswerDTO;
import com.raul.forumhub.topic.dto.response.HttpMessageDefault;
import com.raul.forumhub.topic.service.AnswerService;
import jakarta.validation.Valid;
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

    @PreAuthorize("authenticated")
    @PostMapping("/{topic_id}/answer")
    public ResponseEntity<HttpMessageDefault> answerTopic(@PathVariable Long topic_id, @Valid @RequestBody AnswerTopicDTO answerTopicDTO,
                                                          @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        answerService.answerTopic(topic_id, user_id, answerTopicDTO);

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }

    @PreAuthorize("authenticated")
    @PostMapping("/{topic_id}/answers/{answer_id}")
    public ResponseEntity<HttpMessageDefault> markBetterAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                         @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.markBestAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }

    @PreAuthorize("authenticated and hasAuthority('SCOPE_answer:delete')")
    @DeleteMapping("/{topic_id}/answers/{answer_id}")
    public ResponseEntity<HttpMessageDefault> deleteAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                     @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.deleteAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }

    @PreAuthorize("authenticated and hasAuthority('SCOPE_answer:edit')")
    @PutMapping("/{topic_id}/answers/{answer_id}")
    public ResponseEntity<GetAnswerDTO> updateAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                     @AuthenticationPrincipal Jwt jwt, @RequestBody AnswerUpdateDTO answerUpdateDTO) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        GetAnswerDTO getAnswerDTO = this.answerService.updateAnswer(topic_id, answer_id, user_id, answerUpdateDTO);

        return ResponseEntity.ok(getAnswerDTO);
    }

}
