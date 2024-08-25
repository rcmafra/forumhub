package com.raul.forumhub.user.service;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.UserDetailedInfo;
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


    public void createUser(UserCreateDTO userCreateDTO) {
        Profile basic = this.getProfileByName(Profile.ProfileName.BASIC);
        User user = new User(userCreateDTO);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProfile(basic);

        this.userRepository.save(user);
    }

    public User getInfoUser(Long user_id) {
        return this.getUserById(user_id);
    }

    public Page<UserDetailedInfo> getAllUser(Pageable pageable){
        return this.userRepository.findAll(pageable).map(UserDetailedInfo::new);
    }

    public UserDetailedInfo updateUser(Long user_id, String claimUserRole, UserUpdateDTO userUpdateDTO) {
        User user = this.getUserById(user_id);
        Profile profile = this.getProfileByName(userUpdateDTO.user().getProfile().getProfileName());

        user.setName(userUpdateDTO.user().getName());
        user.setEmail(userUpdateDTO.user().getEmail());

        if(claimUserRole.equals(Profile.ProfileName.ADM.name())){
            user.setProfile(profile);
        }

        userRepository.save(user);
        return new UserDetailedInfo(user);
    }

    public void deleteUser(Long user_id){
        User user = this.getUserById(user_id);
        this.userRepository.delete(user);
    }

    private User getUserById(Long user_id) {
        return this.userRepository.findById(user_id).orElseThrow(() -> new InstanceNotFoundException("Usuário não encontrado"));
    }


    private Profile getProfileByName(Profile.ProfileName profileName){
        return profileRepository.findByProfileName(profileName).orElseThrow();
    }
}
