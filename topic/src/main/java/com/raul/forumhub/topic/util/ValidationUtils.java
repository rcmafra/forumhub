package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.ValidationException;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class ValidationUtils {


    public void validateProvidedAnswer(Set<Answer> answers, Long answer_id) {
        if (!hasAnswer(answers)) {
            throw raiseValidationException("Ainda não existe respostas para esse tópico");
        } else {
            answers.stream().filter(ans -> ans.getId().equals(answer_id))
                    .findFirst().or(() -> {
                        throw raiseValidationException("A resposta fornecida não pertence ao tópico fornecido");
                    }).map(ans -> {
                        if (!isBestAnswer(ans)) {
                            throw raiseValidationException("A resposta fornecida não está definida como melhor resposta");
                        }
                        return ans;
                    });
        }

    }

    public void hasBestAnswer(Set<Answer> answers) {
        if (!hasAnswer(answers)) {
            throw raiseValidationException("Ainda não existe respostas para esse tópico");
        } else {
            answers.stream().filter(Answer::isBestAnswer)
                    .findFirst().ifPresent(a -> {
                        throw new BusinessException(String.format("Este tópico já possui a resposta [ID: %d] como melhor resposta",
                                a.getId()));
                    });
        }
    }

    private boolean isBestAnswer(Answer answer) {
        return answer.isBestAnswer();
    }

    private boolean hasAnswer(Set<Answer> answers) {
        return !answers.isEmpty();
    }

    private ValidationException raiseValidationException(String message) {
        return new ValidationException(message);
    }
}
