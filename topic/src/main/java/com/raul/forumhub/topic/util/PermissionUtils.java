package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Profile;
import com.raul.forumhub.topic.exception.PrivilegeValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PermissionUtils {

    public void privilegeValidator(Long author_id, Author loggedUser) {
        if (!loggedUser.getId().equals(author_id) &&
            !(loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
              loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.ADM))) {
            throw raisePrivilegeValidationException("Usuário com privilégios insuficientes para realizar esta operação!");
        }
    }

    public void validateTopicOwner(Long author_id, Long loggedUser) {
        if (!author_id.equals(loggedUser)) {
            throw raisePrivilegeValidationException("O tópico fornecido não pertence ao usuário atualmente logado");
        }
    }

    private PrivilegeValidationException raisePrivilegeValidationException(String message) {
        return new PrivilegeValidationException(message);
    }

}
