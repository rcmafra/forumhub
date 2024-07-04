package com.backend.api.forumhub.repository;

import com.backend.api.forumhub.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByProfileName(Profile.ProfileName profileName);
}
