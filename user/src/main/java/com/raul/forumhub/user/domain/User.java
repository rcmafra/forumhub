package com.raul.forumhub.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity(name = "users")
@Table(schema = "hub", name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column
    @NotBlank
    private String firstName;
    @Column
    @NotBlank
    private String lastName;
    @Column(unique = true)
    @NotBlank
    private String username;
    @Column(unique = true)
    @Email
    @NotBlank
    private String email;
    @Column
    @JsonIgnore
    private String password;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;

}

