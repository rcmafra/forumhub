package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Profile;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.dto.request.CreateUserDTO;
import com.backend.api.forumhub.dto.request.UpdateUser;
import com.backend.api.forumhub.dto.response.UserResponse;
import com.backend.api.forumhub.repository.ProfileRepository;
import com.backend.api.forumhub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public UserService(UserRepository userRepository, ProfileRepository profileRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    public void createUser(CreateUserDTO createUserDTO) {
        Profile basic = this.getProfileByName(Profile.ProfileName.BASIC);
        User user = new User(createUserDTO);

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setProfile(basic);

        this.userRepository.save(user);
    }

    public User getInfoUser(Long user_id) {
        return this.getUserById(user_id);
    }

    public Page<UserResponse> getAllUser(Pageable pageable){
        return this.userRepository.findAll(pageable).map(UserResponse::new);
    }

    public UserResponse updateUser(Long user_id, UpdateUser updateUser) {
        User user = this.getUserById(user_id);
        Profile profile = this.getProfileByName(updateUser.user().getProfile().getProfileName());

        user.setName(updateUser.user().getName());
        user.setEmail(updateUser.user().getEmail());
        user.setProfile(profile);

        userRepository.save(user);
        return new UserResponse(user);
    }

    /*todo estabaelecer condicação para deleção de usuários*/
    public void deleteUser(Long user_id){
        User user = this.getUserById(user_id);
        this.userRepository.delete(user);
    }

    public User getUserById(Long user_id) {
        return this.userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("User not found"));
    }


    private Profile getProfileByName(Profile.ProfileName profileName){
        return profileRepository.findByProfileName(profileName).orElseThrow();
    }
}
