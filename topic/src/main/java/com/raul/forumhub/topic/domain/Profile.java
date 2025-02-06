package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "profile")
    private ProfileName profileName;

    public enum ProfileName {
        ADM,
        MOD,
        BASIC
    }

}