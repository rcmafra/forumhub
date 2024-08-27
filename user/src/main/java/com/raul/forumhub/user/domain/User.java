package com.raul.forumhub.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Entity(name = "users")
@Table(schema = "hub", name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column
    @NotBlank(message = "Primeiro nome é obrigatório")
    private String firstName;
    @Column
    @NotBlank(message = "Último nome é obrigatório")
    private String lastName;
    @Column
    @NotBlank(message = "Username é obrigatório")
    private String username;
    @Column(unique = true)
    @Email(message = "Formato do email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
    @Column
    @JsonIgnore
    @NotBlank(message = "Senha é obrigatória")
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

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(profile.getProfileName().name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}

