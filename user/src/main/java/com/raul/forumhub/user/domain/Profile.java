package com.raul.forumhub.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;
    @Column(name = "profile", length = 50, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private ProfileName profileName;

    public enum ProfileName {
        ADM,
        MOD,
        BASIC
    }
}



