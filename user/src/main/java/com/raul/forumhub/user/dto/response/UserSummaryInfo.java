package com.raul.forumhub.user.dto.response;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;

public record UserSummaryInfo(Long id, String name, String email, Profile profile) {

    public UserSummaryInfo(User user){
        this(user.getId(), user.getName(), user.getEmail(), user.getProfile());

    }

}
