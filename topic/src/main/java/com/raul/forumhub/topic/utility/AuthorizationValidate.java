package com.raul.forumhub.topic.utility;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Profile;
import com.raul.forumhub.topic.exception.ValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorizationValidate {

    public void permissionValidator(Long author_id, Author user) {
        if (!user.getId().equals(author_id) &&
                !(user.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
                        user.getProfile().getProfileName().equals(Profile.ProfileName.ADM))) {
            throw new ValidationException("Privilégio insuficiente");
        }
    }

}
