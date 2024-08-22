package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;


@Entity
@Table(schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;
    @NotBlank(message = "O curso é obrigatório")
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
