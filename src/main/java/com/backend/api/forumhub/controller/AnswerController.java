package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
import com.backend.api.forumhub.dto.request.NewAnswer;
import com.backend.api.forumhub.dto.response.AnswerDTO;
import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/{topic_id}/answer/")
    private ResponseEntity<HttpMessage> answerTopic(@PathVariable Long topic_id, @Valid @RequestBody AnswerTopicDTO answerTopicDTO,
                                                    @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        answerService.answerTopic(topic_id, user_id, answerTopicDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @PostMapping("/{topic_id}/answers/{answer_id}/")
    private ResponseEntity<HttpMessage> markBetterAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                         @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.markBetterAnswer(topic_id, answer_id, user_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @DeleteMapping("/{topic_id}/answers/{answer_id}/")
    private ResponseEntity<HttpMessage> deleteAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                     @AuthenticationPrincipal Jwt jwt) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.deleteAnswer(topic_id, answer_id, user_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @PutMapping("/{topic_id}/answers/{answer_id}/")
    private ResponseEntity<AnswerDTO> updateAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id,
                                                     @AuthenticationPrincipal Jwt jwt, @RequestBody NewAnswer newAnswer) throws Exception {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        AnswerDTO answerDTO = this.answerService.updateAnswer(topic_id, answer_id, user_id, newAnswer);
        return ResponseEntity.ok(answerDTO);
    }

}
