package com.raul.forumhub.topic.repository;

import com.raul.forumhub.topic.domain.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    Page<Topic> findTopicsByCourseId(Long course_id, Pageable pageable);

}
