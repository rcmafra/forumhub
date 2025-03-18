package com.raul.forumhub.user.dto.response;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;

public record UserSummaryInfo(Long id, String firstName, String lastName, String username, String email,
                              Profile.ProfileName profile) {

    public UserSummaryInfo(User user) {
        this(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail(), user.getProfile().getProfileName());

    }
}
