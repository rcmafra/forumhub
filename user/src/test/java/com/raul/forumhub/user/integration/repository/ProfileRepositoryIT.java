package com.raul.forumhub.user.integration.repository;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.respository.ProfileRepository;
import com.raul.forumhub.user.util.TestsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProfileRepositoryIT {

    @Autowired
    ProfileRepository profileRepository;

    private static boolean hasBeenInitialized = false;

    @BeforeEach
    void setup() {
        if (!hasBeenInitialized) {
            this.profileRepository.saveAll(TestsHelper.ProfileHelper.profileList());
            hasBeenInitialized = true;
        }
    }

    @Test
    void shouldFindProfileByNameWithSuccessIfExists() {
        assertTrue(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC)
                .isPresent());

    }

    @Test
    void shouldReturnOptionalEmptyWhenSearchProfileByNameAndHimNotExists() {
        this.profileRepository.deleteById(2L);

        assertTrue(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC)
                .isEmpty());

    }
}
