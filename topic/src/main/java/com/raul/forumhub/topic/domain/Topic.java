package com.raul.forumhub.topic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    public Topic(String title, String question, Author author, Course course){
        this.title = title;
        this.question = question;
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.course = course;
        this.status = Status.UNSOLVED;
    }

    public Topic(Long id, String title, String question, LocalDateTime createdAt, Status status,
                 Author author, Course course){
        this.id = id;
        this.title = title;
        this.question = question;
        this.createdAt = createdAt;
        this.status = status;
        this.author = author;
        this.course = course;
    }

    public Topic(String title, String question, Status status){
        this.title = title;
        this.question = question;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", question='" + question + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", author=" + author +
                ", course=" + course +
                ", answers=" + answers +
                '}';
    }
}
