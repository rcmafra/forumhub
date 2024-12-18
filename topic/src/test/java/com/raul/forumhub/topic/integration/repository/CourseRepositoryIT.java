package com.raul.forumhub.topic.integration.repository;

import com.raul.forumhub.topic.repository.CourseRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CourseRepositoryIT {

    @Autowired
    CourseRepository courseRepository;


    @Test
    void shouldFindCourseByNameWithSuccessIfExists() {
        this.courseRepository.save(TestsHelper.CourseHelper.courseList().get(0));

        assertThat(this.courseRepository.findCourseByName("Criação de uma API Rest")
                .isPresent());

    }

    @Test
    void shouldReturnOptionalEmptyWhenSearchCourseByNameAndHimNotExists() {
        this.courseRepository.saveAll(TestsHelper.CourseHelper.courseList());

        assertThat(this.courseRepository.findCourseByName("Aprendendo sobre mocks")
                .isEmpty());

    }
}