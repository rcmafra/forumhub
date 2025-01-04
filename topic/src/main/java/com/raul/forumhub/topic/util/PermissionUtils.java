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

    public void privilegeValidator(Long author_id, Author user) {
        if (!user.getId().equals(author_id) &&
                !(user.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
                        user.getProfile().getProfileName().equals(Profile.ProfileName.ADM))) {
            throw new ValidationException("Privilégio insuficiente");
        }
        log.info("Privilege validator ==> Passou pela validação de privilégio");
    }

    public void validateTopicOwner(Long author_id, Long user_id) {
        if (!author_id.equals(user_id)) {
            throw new TopicServiceException("O tópico fornecido não pertence a esse autor");
        }
        log.info("Validate topic owner ==> Passou pela validação de autor proprietário do tópico");
    }

}
