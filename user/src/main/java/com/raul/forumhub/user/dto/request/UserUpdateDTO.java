package com.raul.forumhub.user.dto.request;

import com.raul.forumhub.user.domain.Profile;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(@NotBlank(message = "O username não pode ser vazio") String username,
                            @NotBlank(message = "O email não pode ser vazio") String email,
                            Profile.ProfileName profileName, boolean accountNonExpired, boolean accountNonLocked,
                            boolean credentialsNonExpired, boolean enabled) {
}
