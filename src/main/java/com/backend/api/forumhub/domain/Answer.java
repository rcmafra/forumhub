package com.backend.api.forumhub.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumns(value = @JoinColumn(name = "topic_id"), foreignKey = @ForeignKey(name = "topic_id"))
    @JsonIgnore
    private Topic topic;
    @Column
    @NotBlank(message = "Solution cannot be left blank")
    private String solution;
    @Column
    private boolean betterAnswer;
    @Column
    private LocalDateTime createdAt;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumns(value = @JoinColumn(name = "user_id"), foreignKey = @ForeignKey(name = "user_id"))
    private User author;

    public Answer(Topic topic, String solution, User author){
        this.topic = topic;
        this.solution = solution;
        this.createdAt = LocalDateTime.now();
        this.author = author;
    }

}
