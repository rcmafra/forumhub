package com.backend.api.forumhub.repository;

import com.backend.api.forumhub.domain.Topic;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface TopicRepository extends JpaRepository<Topic, Long> {
}
