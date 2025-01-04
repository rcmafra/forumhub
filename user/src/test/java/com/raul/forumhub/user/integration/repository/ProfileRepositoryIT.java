package com.raul.forumhub.user.integration.repository;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.respository.ProfileRepository;
import com.raul.forumhub.user.util.TestsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ProfileRepositoryIT {

    @Autowired
    ProfileRepository profileRepository;

    @Test
    void shouldFindProfileByNameWithSuccessIfExists() {
        this.profileRepository.save(TestsHelper.ProfileHelper.profileList().get(0));

        assertThat(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC)
                .isPresent());

    }

    @Test
    void shouldReturnOptionalEmptyWhenSearchCourseByNameAndHimNotExists() {
        this.profileRepository.save(TestsHelper.ProfileHelper.profileList().get(1));

        assertThat(this.profileRepository.findByProfileName(Profile.ProfileName.ADM)
                .isEmpty());

    }
}
