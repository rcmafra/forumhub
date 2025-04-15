package com.raul.forumhub.topic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = @JoinColumn(name = "topic_id"), foreignKey = @ForeignKey(name = "topic_id"))
    @JsonIgnore
    @ToString.Exclude
    private Topic topic;
    @NotBlank
    @Column(nullable = false)
    private String solution;
    @Column
    private boolean bestAnswer;
    @Column
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(value = @JoinColumn(name = "user_id"), foreignKey = @ForeignKey(name = "user_id"))
    private Author author;

}
