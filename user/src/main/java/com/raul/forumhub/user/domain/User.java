package com.raul.forumhub.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column
    @NotBlank(message = "O primeiro nome não pode ser vazio")
    private String firstName;
    @Column
    @NotBlank(message = "O ultimo nome não pode ser vazio")
    private String lastName;
    @Column
    @NotBlank(message = "O username não pode ser vazio")
    private String username;
    @Column(unique = true)
    @Email(message = "Formato do email inválido")
    @NotBlank(message = "O email não pode ser vazio")
    private String email;
    @Column
    @JsonIgnore
    @NotBlank(message = "A senha não pode ser vazia")
    private String password;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;

    public User(UserCreateDTO userCreateDTO){
        this.firstName = userCreateDTO.firstName();
        this.lastName = userCreateDTO.lastName();
        this.username = userCreateDTO.username();
        this.email = userCreateDTO.email();
        this.password = userCreateDTO.password();
        this.isAccountNonExpired = true;
        this.isAccountNonLocked = true;
        this.isCredentialsNonExpired = true;
        this.isEnabled = true;
    }

    public boolean getIsAccountNonExpired() {
        return isAccountNonExpired;
    }

    public boolean getIsAccountNonLocked() {
        return isAccountNonLocked;
    }

    public boolean getIsCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

}

