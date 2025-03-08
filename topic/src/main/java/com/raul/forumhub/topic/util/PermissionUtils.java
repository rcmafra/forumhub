package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Profile;
import com.raul.forumhub.topic.exception.TopicServiceException;
import com.raul.forumhub.topic.exception.ValidationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class PermissionUtils {

    public void privilegeValidator(Long author_id, Author loggedUser) {
        if (!loggedUser.getId().equals(author_id) &&
                !(loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
                        loggedUser.getProfile().getProfileName().equals(Profile.ProfileName.ADM))) {
            throw new ValidationException("Privilégio insuficiente");
        }
        log.info("Privilege validator ==> Passou pela validação de privilégio");
    }

    public void validateTopicOwner(Long author_id, Long loggedUser) {
        if (!author_id.equals(loggedUser)) {
            throw new TopicServiceException("O tópico fornecido não pertence ao autor atualmente logado");
        }
        log.info("Validate topic owner ==> Passou pela validação de autor proprietário do tópico");
    }

}
