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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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


    public void registerUser(UserCreateDTO userCreateDTO) {
        Profile profile = this.findProfileByName(Profile.ProfileName.BASIC);
        User user = User.builder()
                .firstName(userCreateDTO.firstName())
                .lastName(userCreateDTO.lastName())
                .username(userCreateDTO.username())
                .email(userCreateDTO.email().toLowerCase())
                .password(userCreateDTO.password())
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        user.setProfile(profile);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        this.userRepository.save(user);
    }

    public User getDetailedInfoUser(Long user_id) {
        return this.getUserById(user_id);
    }

    public Page<UserSummaryInfo> usersList(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(UserSummaryInfo::new);
    }

    public UserDetailedInfo updateUser(Long user_id, Profile.ProfileName claimUserRole, UserUpdateDTO userUpdateDTO) {
        User user = this.getUserById(user_id);
        Profile profile = this.findProfileByName(userUpdateDTO.profileName());

        user.setUsername(userUpdateDTO.username());
        user.setEmail(userUpdateDTO.email());

        if (claimUserRole.equals(Profile.ProfileName.ADM)) {
            User.builder().firstName(userUpdateDTO.firstName())
                    .lastName(userUpdateDTO.lastName())
                    .profile(profile)
                    .isAccountNonExpired(userUpdateDTO.accountNonExpired())
                    .isAccountNonLocked(userUpdateDTO.accountNonLocked())
                    .isCredentialsNonExpired(userUpdateDTO.credentialsNonExpired())
                    .isEnabled(userUpdateDTO.enabled())
                    .build();
        }

        userRepository.save(user);
        return new UserDetailedInfo(user);
    }

    public void deleteUser(Long user_id) {
        User user = this.getUserById(user_id);
        this.userRepository.delete(user);
    }

    private User getUserById(Long user_id) {
        return this.userRepository.findById(user_id).orElseThrow(() -> new InstanceNotFoundException("Usuário não encontrado"));
    }


    public Profile findProfileByName(Profile.ProfileName profileName) {
        return profileRepository.findByProfileName(profileName).orElseThrow(() -> new InstanceNotFoundException("Perfil não encontrado"));
    }
}
