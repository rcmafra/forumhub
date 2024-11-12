Adipackage com.raul.forumhub.topic.repository;

import com.raul.forumhub.topic.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
