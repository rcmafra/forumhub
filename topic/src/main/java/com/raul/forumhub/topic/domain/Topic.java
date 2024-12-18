package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;
    @NotBlank(message = "O título não pode ser vazio")
    @Column(length = 150)
    private String title;
    @NotBlank(message = "A pergunta não pode ser vazia")
    @Column
    private String question;
    @Column
    private LocalDateTime createdAt;
    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user_id")))
    private Author author;
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "course_id")))
    private Course course;
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Answer> answers;

    public Topic(String title, String question, Author author, Course course) {
        this.title = title;
        this.question = question;
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.course = course;
        this.status = Status.UNSOLVED;
    }
}
