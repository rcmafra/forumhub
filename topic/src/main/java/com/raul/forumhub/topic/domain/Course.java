package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(schema = "hub")
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

    public Course(String name, Category category){
        this.name = name;
        this.category = category;
    }

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
