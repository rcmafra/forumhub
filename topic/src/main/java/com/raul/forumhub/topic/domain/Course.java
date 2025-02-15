package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Category category;

    public enum Category {

        JAVA,
        C,
        CPLUSPLUS,
        CSHARP,
        GOLANG,
        COMPUTATION,
        QA

    }

}
