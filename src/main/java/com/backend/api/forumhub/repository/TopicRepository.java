package com.backend.api.forumhub.repository;

import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

     Optional<List<Topic>> getTopicByAuthor(User user_id);
}
