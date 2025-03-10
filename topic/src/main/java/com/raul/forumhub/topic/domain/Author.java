package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity(name = "users")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @NotNull(message = "O id do usuário não pode ser nulo")
    private Long id;
    @NotNull(message = "O username do usuário não pode ser nulo")
    private String username;
    @NotNull(message = "O email do usuário não pode ser nulo")
    private String email;
    @NotNull(message = "O perfil do usuário não pode ser nulo")
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;
}
