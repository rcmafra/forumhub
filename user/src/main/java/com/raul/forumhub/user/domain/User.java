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


@Entity(name = "users")
@Table(schema = "hub", name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public User(UserCreateDTO userCreateDTO){
        this.name = userCreateDTO.name();
        this.email = userCreateDTO.email();
        this.password = userCreateDTO.password();
    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(Profile profile){
        this.profile = profile;
    }

}

