package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.exception.AnswerServiceException;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class ValidationUtils {

    public void hasBestAnswer(Set<Answer> answers) {
        if (answers.isEmpty()) {
            throw new AnswerServiceException("Ainda não existe respostas para esse tópico");
        }
        answers.stream().filter(Answer::isBestAnswer)
                .findFirst().ifPresent(a -> {
                    throw new AnswerServiceException(String.format("Este tópico já possui a resposta [ID: %d] como melhor resposta",
                            a.getId()));

                });
    }
}
