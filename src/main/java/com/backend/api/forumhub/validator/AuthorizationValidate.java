package com.backend.api.forumhub.validator;

import com.backend.api.forumhub.domain.Profile;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorizationValidate {

    public static void AuthValidator(Long author_id, Long user_id, Profile.ProfileName role){
        if(!user_id.equals(author_id) && !(role.equals(Profile.ProfileName.MOD) || role.equals(Profile.ProfileName.ADM))){
            throw new RuntimeException("Insufficient privilege");
        }
    }

}
