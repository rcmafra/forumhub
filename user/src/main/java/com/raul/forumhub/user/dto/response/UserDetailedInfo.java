package com.raul.forumhub.user.dto.response;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;

public record UserDetailedInfo(Long id,
                               String firstName,
                               String lastName,
                               String username,
                               String email,
                               Profile.ProfileName profile,
                               boolean accountNonExpired,
                               boolean accountNonLocked,
                               boolean credentialsNonExpired,
                               boolean enabled) {

    public UserDetailedInfo(User user) {
        this(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile().getProfileName(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.isEnabled()
        );

    }
}



