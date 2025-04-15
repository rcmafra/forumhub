package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.ValidationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@UtilityClass
public class ValidationUtils {

    public void validateMarkBestAnswer(Topic topic, Answer answer) {
        hasAnswerInTopic(topic);
        validateAnswerBelongsTopic(topic, answer);

        topic.getAnswers().stream().filter(Answer::isBestAnswer)
                .findFirst().ifPresent(ans -> {
                    throw new BusinessException(String.format("Este tópico já possui como melhor resposta a resposta [ID: %d]",
                            ans.getId()));
                });

        log.info("Validações realizadas com sucesso para marcação de melhor resposta para a resposta: {}", answer);

    }


    public void validateUnmarkBestAnswer(Topic topic, Answer answer) {
        hasAnswerInTopic(topic);
        validateAnswerBelongsTopic(topic, answer);

        if(!answer.isBestAnswer()) {
            throw raiseValidationException(String.format(
                    "A resposta [ID: %d] fornecida não está definida como melhor resposta", answer.getId()));
        }

        log.info("Validações realizadas com sucesso para desmarcação de melhor resposta para a resposta: {}", answer);

    }

    private void hasAnswerInTopic(Topic topic) {
        if (!hasAnswer(topic.getAnswers())) {
            throw raiseValidationException(String.format("Ainda não existe respostas para o tópico [ID: %d]", topic.getId()));
        }

    }


    private boolean hasAnswer(Set<Answer> answers) {
        return !answers.isEmpty();
    }


    public void validateAnswerBelongsTopic(Topic topic, Answer answer) {
        if (!answer.getTopic().getId().equals(topic.getId())) {
            throw raiseValidationException(String.format(
                    "A resposta [ID: %d] fornecida não pertence ao tópico [ID: %d] fornecido", answer.getId(), topic.getId()));
        }
    }

    private ValidationException raiseValidationException(String message) {
        return new ValidationException(message);
    }
}
