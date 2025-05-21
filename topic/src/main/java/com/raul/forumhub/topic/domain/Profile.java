package com.raul.forumhub.topic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @ToString.Exclude
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "profile", length = 50)
    private ProfileName profileName;

    public enum ProfileName {
        ADM,
        MOD,
        BASIC,
        ANONYMOUS
    }

}