package com.raul.forumhub.user.dto.request;

import com.raul.forumhub.user.domain.Profile;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(@NotBlank(message = "O username não pode ser vazio") String username,
                            @NotBlank(message = "O email não pode ser vazio") String email,
                            @NotBlank(message = "O perfil não pode ser vazio") Profile.ProfileName profileName,
                            @NotBlank(message = "conta não expirada não pode ser vazia") boolean accountNonExpired,
                            @NotBlank(message = "conta não bloqueada não pode ser vazia") boolean accountNonLocked,
                            @NotBlank(message = "credenciais não expiradas não pode ser vazia") boolean credentialsNonExpired,
                            @NotBlank(message = "conta habilitada não pode ser vazia") boolean enabled) {
}
