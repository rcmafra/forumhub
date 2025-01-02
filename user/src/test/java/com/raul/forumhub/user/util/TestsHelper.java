package com.raul.forumhub.user.util;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;

import java.util.ArrayList;
import java.util.List;

public class TestsHelper {

    public static final class UserHelper {
        public static List<User> userList() {
            final List<User> authorList = new ArrayList<>();
            authorList.add(User.builder()
                    .id(1L)
                    .firstName("Jose")
                    .lastName("Silva")
                    .username("jose_silva")
                    .email("jose@email.com")
                    .password("password")
                    .profile(ProfileHelper.profileList().get(0))
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .build()
            );
            authorList.add(User.builder()
                    .id(2L)
                    .firstName("Maria")
                    .lastName("Silva")
                    .username("maria_silva")
                    .email("maria@email.com")
                    .password("password")
                    .profile(ProfileHelper.profileList().get(1))
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .build()
            );
            authorList.add(User.builder()
                    .id(3L)
                    .firstName("Joao")
                    .lastName("Silva")
                    .username("joao_silva")
                    .email("joao@email.com")
                    .password("password")
                    .profile(ProfileHelper.profileList().get(2))
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .build()
            );
            return authorList;
        }


    }

    public static final class ProfileHelper {
        public static List<Profile> profileList() {
            final List<Profile> profileList = new ArrayList<>();
            profileList.add(Profile.builder()
                    .id(1L)
                    .profileName(Profile.ProfileName.BASIC)
                    .build()
            );
            profileList.add(Profile.builder()
                    .id(2L)
                    .profileName(Profile.ProfileName.MOD)
                    .build()
            );
            profileList.add(Profile.builder()
                    .id(3L)
                    .profileName(Profile.ProfileName.ADM)
                    .build()
            );
            return profileList;
        }


    }
}
