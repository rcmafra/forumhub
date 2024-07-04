package com.backend.api.forumhub.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    @Column(length = 200)
    @NotBlank(message = "Title is mandatory")
    private String title;
    @Column
    @NotBlank(message = "Message is mandatory")
    private String message;
    @Column
    private LocalDateTime createdAt;
    @Column(name = "status", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user_id")))
    private User author;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumns(@JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "course_id")))
    private Course course;
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumns(@JoinColumn(name = "answer_id", foreignKey = @ForeignKey(name = "answer_id")))
    private List<Answer> answer;

    public Topic(String title, String message, User author, Course course){
        this.title = title;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.course = course;
        this.status = Status.UNSOLVED;
    }

    public Topic(String title, String message, Status status){
        this.title = title;
        this.message = message;
        this.status = status;
    }


}
