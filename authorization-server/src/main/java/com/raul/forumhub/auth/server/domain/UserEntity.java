package com.raul.forumhub.auth.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "profile_id")))
    private Profile profile;

}
