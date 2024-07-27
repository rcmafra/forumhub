package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Answer;
import com.backend.api.forumhub.domain.Profile;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.dto.request.CreateUserDTO;
import com.backend.api.forumhub.dto.request.UpdateUser;
import com.backend.api.forumhub.dto.response.UserResponse;
import com.backend.api.forumhub.repository.AnswerRepository;
import com.backend.api.forumhub.repository.ProfileRepository;
import com.backend.api.forumhub.repository.TopicRepository;
import com.backend.api.forumhub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final TopicRepository topicRepository;

    private final AnswerRepository answerRepository;


    public UserService(UserRepository userRepository, ProfileRepository profileRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                       TopicRepository topicRepository, AnswerRepository answerRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.topicRepository = topicRepository;
        this.answerRepository = answerRepository;
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

    public UserResponse updateUser(Long user_id, String claimUserRole, UpdateUser updateUser) {
        User user = this.getUserById(user_id);
        Profile profile = this.getProfileByName(updateUser.user().getProfile().getProfileName());

        user.setName(updateUser.user().getName());
        user.setEmail(updateUser.user().getEmail());

        if(claimUserRole.equals(Profile.ProfileName.ADM.name())){
            user.setProfile(profile);
        }

        userRepository.save(user);
        return new UserResponse(user);
    }

    public void deleteUser(Long user_id){
        User user = this.getUserById(user_id);
        List<Topic> topics = this.topicRepository.getTopicByAuthor(user).orElse(Collections.emptyList());
        List<Answer> answers = this.answerRepository.getAnswerByAuthor(user).orElse(Collections.emptyList());

        this.topicRepository.deleteAll(topics);
        this.answerRepository.deleteAll(answers);
        this.userRepository.delete(user);
    }

    public User getUserById(Long user_id) {
        return this.userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("User not found"));
    }


    private Profile getProfileByName(Profile.ProfileName profileName){
        return profileRepository.findByProfileName(profileName).orElseThrow();
    }
}
