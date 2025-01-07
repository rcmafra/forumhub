package com.raul.forumhub.authorization.server.security.userdetails;

import com.raul.forumhub.authorization.server.domain.UserEntity;
import com.raul.forumhub.authorization.server.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
    public UserDetails loadUserByUsername(String email) {
        final UserEntity userEntity = this.userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new User(userEntity.getEmail(), userEntity.getPassword(), userEntity.getIsEnabled(),
                userEntity.getIsAccountNonExpired(), userEntity.getIsCredentialsNonExpired(), userEntity.getIsAccountNonLocked(),
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getProfile().getProfileName().name())));

    }


}
