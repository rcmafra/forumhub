package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-forum/v1/forumhub/topics")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService){
        this.answerService = answerService;
    }

    @PostMapping("/{topic_id}/answer/")
    private ResponseEntity<HttpMessage> answerTopic(@PathVariable Long topic_id, @Valid @RequestBody AnswerTopicDTO answerTopicDTO) throws Exception {
        answerService.answerTopic(topic_id, answerTopicDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @PostMapping("/{topic_id}/answers/{answer_id}/")
    private ResponseEntity<HttpMessage> markBetterAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id) throws Exception {
        this.answerService.markBetterAnswer(topic_id, answer_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }

    @DeleteMapping("/{topic_id}/answers/{answer_id}/")
    private ResponseEntity<HttpMessage> deleteAnswer(@PathVariable Long topic_id, @PathVariable Long answer_id) throws Exception {
        this.answerService.deleteAnswer(topic_id, answer_id);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.OK);
    }


}
