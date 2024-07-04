package com.backend.api.forumhub.domain;

import com.backend.api.forumhub.dto.request.CreateUserDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    private Long id;
    @Column
    @NotBlank(message = "Name is mandatory")
    private String name;
    @Column(unique = true)
    @Email(message = "Email format invalid")
    @NotBlank(message = "Email is mandatory")
    private String email;
    @Column
    @NotBlank(message = "Password is mandatory")
    @JsonIgnore
    private String password;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;

    public User(CreateUserDTO createUserDTO){
        this.name = createUserDTO.name();
        this.email = createUserDTO.email();
        this.password = createUserDTO.password();
    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(Profile profile){
        this.profile = profile;
    }

}
