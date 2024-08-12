package com.raul.forumhub.auth.server.security.userdetails;

import com.raul.forumhub.auth.server.domain.UserEntity;
import com.raul.forumhub.auth.server.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final UserEntity userEntity = this.userRepository.findByEmail(email.toLowerCase()).orElseThrow();

        return new org.springframework.security.core.userdetails.User(
                userEntity.getEmail(), userEntity.getPassword(), Collections.singleton(
                        new SimpleGrantedAuthority(userEntity.getProfile().getProfileName().name()))
        );
    }
}
