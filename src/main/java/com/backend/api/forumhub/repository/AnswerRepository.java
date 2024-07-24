package com.backend.api.forumhub.repository;

import com.backend.api.forumhub.domain.Answer;
import com.backend.api.forumhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<List<Answer>> getAnswerByAuthor(User user_id);
}
