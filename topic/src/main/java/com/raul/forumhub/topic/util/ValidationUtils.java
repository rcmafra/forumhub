package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.ValidationException;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class ValidationUtils {

    public void validateMarkBestAnswer(Topic topic, Long answer_id) {
        hasAnswerInTopic(topic);

        topic.getAnswers().stream().filter(ans -> ans.getId().equals(answer_id))
                .findFirst().or(() -> {
                            throw raiseValidationException("A resposta fornecida não pertence ao tópico fornecido");
                        }
                );

        topic.getAnswers().stream().filter(Answer::isBestAnswer)
                .findFirst().ifPresent(ans -> {
                    throw new BusinessException(String.format("Este tópico já possui a resposta [ID: %d] como melhor resposta",
                            ans.getId()));
                });

    }


    public void validateUnmarkBestAnswer(Topic topic, Long answer_id) {
        hasAnswerInTopic(topic);

        topic.getAnswers().stream().filter(ans -> ans.getId().equals(answer_id))
                .findFirst().or(() -> {
                    throw raiseValidationException("A resposta fornecida não pertence ao tópico fornecido");
                }).ifPresent(ans -> {
                    if (!ans.isBestAnswer()) {
                        throw raiseValidationException("A resposta fornecida não está definida como melhor resposta");
                    }
                });

    }

    private void hasAnswerInTopic(Topic topic) {
        if (!hasAnswer(topic.getAnswers())) {
            throw raiseValidationException("Ainda não existe respostas para esse tópico");
        }

    }


    private boolean hasAnswer(Set<Answer> answers) {
        return !answers.isEmpty();
    }

    private ValidationException raiseValidationException(String message) {
        return new ValidationException(message);
    }
}
