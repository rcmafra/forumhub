package com.raul.forumhub.authorization.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    @JsonIgnore
    private Long id;
    @Column(name = "profile")
    @Enumerated(EnumType.STRING)
    private ProfileName profileName;

    public enum ProfileName {

        ADM,
        MOD,
        BASIC,
        ANONYMOUS
    }
}



