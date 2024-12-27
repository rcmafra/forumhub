package com.raul.forumhub.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateDTO(@NotBlank(message = "O primeiro nome não pode ser vazio") String firstName,
                            @NotBlank(message = "O sobrenome não pode ser vazio") String lastName,
                            @NotBlank(message = "O username não pode ser vazio") String username,
                            @NotBlank(message = "O email não pode ser vazio") @Email(message = "Formato do email inválido") String email,
                            @NotBlank(message = "A senha não pode ser vazia") String password){
}
