package com.raul.forumhub.user.service;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.UserDetailedInfo;
import com.raul.forumhub.user.dto.response.UserSummaryInfo;
import com.raul.forumhub.user.exception.InstanceNotFoundException;
import com.raul.forumhub.user.respository.ProfileRepository;
import com.raul.forumhub.user.respository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserDetailedInfo registerUser(UserCreateDTO userCreateDTO) {
        Profile profile = this.findProfileByName(Profile.ProfileName.BASIC);
        User user = User.builder()
                .firstName(userCreateDTO.firstName())
                .lastName(userCreateDTO.lastName())
                .username(userCreateDTO.username())
                .profile(profile)
                .email(userCreateDTO.email().toLowerCase())
                .password(passwordEncoder.encode(userCreateDTO.password()))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        this.userRepository.save(user);

        log.info("Usuário '{}' criado com sucesso!", user.getUsername());

        return new UserDetailedInfo(user);
    }

    public UserDetailedInfo getDetailedInfoUser(Long user_id) {
        return new UserDetailedInfo(this.getUserById(user_id));
    }

    public Page<UserSummaryInfo> usersList(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(UserSummaryInfo::new);
    }

    public UserDetailedInfo updateUser(Long user_id, Profile.ProfileName claimUserRole, UserUpdateDTO userUpdateDTO) {
        User user = this.getUserById(user_id);
        Profile profile = this.findProfileByName(userUpdateDTO.profile());

        user.setUsername(userUpdateDTO.username());
        user.setEmail(userUpdateDTO.email());

        if (claimUserRole.equals(Profile.ProfileName.ADM)) {
            user.setFirstName(userUpdateDTO.firstName());
            user.setLastName(userUpdateDTO.lastName());
            user.setProfile(profile);
            user.setAccountNonExpired(userUpdateDTO.accountNonExpired());
            user.setAccountNonLocked(userUpdateDTO.accountNonLocked());
            user.setCredentialsNonExpired(userUpdateDTO.credentialsNonExpired());
            user.setEnabled(userUpdateDTO.enabled());
        }

        userRepository.save(user);

        log.info("Usuário [ID: {}] editado com sucesso!", user.getId());

        return new UserDetailedInfo(user);
    }

    public void deleteUser(Long user_id) {
        User user = this.getUserById(user_id);

        log.info("Usuário '{}' removido com sucesso!", user.getUsername());

        this.userRepository.delete(user);
    }

    public User getUserById(Long user_id) {
        return this.userRepository.findById(user_id).filter(user -> !user.getId().equals(0L))
                .orElseThrow(() -> new InstanceNotFoundException(String.format("Usuário [ID: %d] não encontrado", user_id)));
    }


    public Profile findProfileByName(Profile.ProfileName profileName) {
        return profileRepository.findByProfileName(profileName).orElseThrow(
                () -> new InstanceNotFoundException(String.format("Perfil '%s' não encontrado", profileName.toString())));
    }
}
