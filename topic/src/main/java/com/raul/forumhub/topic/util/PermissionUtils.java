package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Profile;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.exception.PrivilegeValidationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class PermissionUtils {

    public void privilegeValidator(Long author_id, Author loggedUser) {
        if (!loggedUser.getId().equals(author_id) &&
            !(loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
              loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.ADM))) {
            throw raisePrivilegeValidationException(String.format(
                    "Usuário '%s' com privilégios insuficientes para realizar esta operação!", loggedUser.getUsername()));
        }
        log.info("Usuário '{}' com privilégios suficientes para realizar a operação!", loggedUser.getUsername());
    }

    public void validateTopicOwner(Topic topic, Author loggedUser) {
        if (!topic.getAuthor().getId().equals(loggedUser.getId())) {
            throw raisePrivilegeValidationException(String.format(
                    "O tópico [ID: %d] fornecido não pertence ao usuário '%s'", topic.getId(), loggedUser.getUsername()));
        }
        log.info("O tópico pertence ao usuário '{}'. Permite prosseguir com a operação!", loggedUser.getUsername());
    }

    private PrivilegeValidationException raisePrivilegeValidationException(String message) {
        return new PrivilegeValidationException(message);
    }

}
