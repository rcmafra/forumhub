package com.raul.forumhub.user.dto.request;

import com.raul.forumhub.user.domain.Profile;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(@NotBlank(message = "O primeiro nome não pode ser vazio") String firstName,
                            @NotBlank(message = "O sobrenome não pode ser vazio") String lastName,
                            @NotBlank(message = "O username não pode ser vazio") String username,
                            @NotBlank(message = "O email não pode ser vazio") String email,
                            Profile.ProfileName profileName, boolean accountNonExpired, boolean accountNonLocked,
                            boolean credentialsNonExpired, boolean enabled) {
}
