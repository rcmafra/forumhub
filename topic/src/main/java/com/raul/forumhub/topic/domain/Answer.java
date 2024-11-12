package com.raul.forumhub.topic.domain;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = @JoinColumn(name = "topic_id"), foreignKey = @ForeignKey(name = "topic_id"))
    @JsonIgnore
    private Topic topic;
    @NotBlank(message = "A solução não pode ser vazia")
    @Column(nullable = false)
    private String solution;
    @Column
    private boolean bestAnswer;
    @Column
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(value = @JoinColumn(name = "user_id"), foreignKey = @ForeignKey(name = "user_id"))
    private Author author;

    public Answer(Long id, String solution, boolean bestAnswer, LocalDateTime createdAt, Author author){
        this.id = id;
        this.solution = solution;
        this.bestAnswer = bestAnswer;
        this.createdAt = createdAt;
        this.author = author;
    }

    public Answer(String solution){
        this.solution = solution;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", topic=" + topic +
                ", solution='" + solution + '\'' +
                ", bestAnswer=" + bestAnswer +
                ", createdAt=" + createdAt +
                ", author=" + author +
                '}';
    }
}
