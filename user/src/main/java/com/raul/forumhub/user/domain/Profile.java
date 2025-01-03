package com.raul.forumhub.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "profile", length = 50, nullable = false, unique = true)
    private ProfileName profileName;

    public enum ProfileName {
        ADM,
        MOD,
        BASIC
    }
}



