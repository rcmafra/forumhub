package com.raul.forumhub.user.service;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.UserSummaryInfo;
import com.raul.forumhub.user.exception.InstanceNotFoundException;
import com.raul.forumhub.user.respository.ProfileRepository;
import com.raul.forumhub.user.respository.UserRepository;
import com.raul.forumhub.user.util.TestsHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    ProfileRepository profileRepository;

    @Mock
    PasswordEncoder passwordEncoder;


    @Test
    void shouldFailToCreateUserIfBasicProfileNotExists() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willThrow(new InstanceNotFoundException(String.format("Perfil '%s' não encontrado",
                        Profile.ProfileName.BASIC.name())));

        assertThrows(InstanceNotFoundException.class, () -> this.userService.registerUser(userCreateDTO),
                "Perfil 'BASIC' não encontrado");

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verifyNoInteractions(passwordEncoder, userRepository);
        BDDMockito.verifyNoMoreInteractions(profileRepository);

    }

    @Test
    void shouldFailToCreateUserIfAlreadyExistsAnUserWithSameUsername() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "jose_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.of(new Profile(2L, Profile.ProfileName.BASIC)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        BDDMockito.given(this.userRepository.save(any(User.class)))
                .willThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldFailToCreateUserIfAlreadyExistsAnUserWithSameEmail() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "jose_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.of(new Profile(2L, Profile.ProfileName.BASIC)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        BDDMockito.given(this.userRepository.save(any(User.class)))
                .willThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldFailToCreateUserIfFirstNameLengthOvertake255Chars() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO(
                "User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+-=[]{}|;:,." +
                "<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+-=[]{}|;:," +
                ".<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^",
                "Silva", "jose_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.of(new Profile(2L, Profile.ProfileName.BASIC)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        BDDMockito.given(this.userRepository.save(any(User.class)))
                .willThrow(JpaSystemException.class);

        assertThrows(JpaSystemException.class, () -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldFailToCreateUserIfLastNameLengthOvertake255Chars() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+-=[]{}|;:,." +
                "<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+-=[]{}|;:,." +
                "<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^",
                "jose_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.of(new Profile(2L, Profile.ProfileName.BASIC)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        BDDMockito.given(this.userRepository.save(any(User.class)))
                .willThrow(JpaSystemException.class);

        assertThrows(JpaSystemException.class, () -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldFailToCreateUserIfUsernameLengthOvertake255Chars() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUV" +
                         "WXYZ!@#$%^&*()_+-=[]{}|;:,.<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRST" +
                         "UVWXYZ!@#$%^&*()_+-=[]{}|;:,.<>?~User1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRST" +
                         "UVWXYZ!@#$%", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.of(new Profile(2L, Profile.ProfileName.BASIC)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        BDDMockito.given(this.userRepository.save(any(User.class)))
                .willThrow(JpaSystemException.class);

        assertThrows(JpaSystemException.class, () -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldCreateUserWithSuccessIfEverythingIsOk() {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "jose_silva", "marcus@email.com",
                "P4s$word");

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willReturn(Optional.ofNullable(TestsHelper.ProfileHelper.profileList().get(1)));

        BDDMockito.given(this.passwordEncoder.encode(any(String.class)))
                .willReturn("encrypted-password-test");

        assertDoesNotThrow(() -> this.userService.registerUser(userCreateDTO));

        BDDMockito.verify(profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(passwordEncoder).encode(any(String.class));
        BDDMockito.verify(userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.passwordEncoder);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);


    }

    @Test
    void shouldFailToRequestDetailedInfoUserIfHimNotExists() {
        BDDMockito.given(this.userRepository.findById(5L))
                .willThrow(new InstanceNotFoundException(String.format("Usário [ID: %d] não encontrado", 5L)));

        assertThrows(InstanceNotFoundException.class, () -> this.userService.getDetailedInfoUser(5L),
                "Usuário [ID: 5] não encontrado");

        BDDMockito.verify(userRepository).findById(5L);
        BDDMockito.verifyNoMoreInteractions(userRepository);

    }

    @Test
    void shouldToReturnDetailedInfoUserWithSuccessIfExists() {
        User user = TestsHelper.UserHelper.userList().get(1);

        BDDMockito.given(this.userRepository.findById(2L)).willReturn(Optional.of(user));

        assertDoesNotThrow(() -> this.userService.getDetailedInfoUser(2L));

        assertAll(
                () -> assertEquals(2L, user.getId()),
                () -> assertEquals("Jose", user.getFirstName()),
                () -> assertEquals("Silva", user.getLastName()),
                () -> assertEquals("jose_silva", user.getUsername()),
                () -> assertEquals("jose@email.com", user.getEmail()),
                () -> assertEquals("password", user.getPassword()),
                () -> assertEquals(Profile.ProfileName.BASIC, user.getProfile().getProfileName()),
                () -> assertTrue(user.isAccountNonExpired()),
                () -> assertTrue(user.isAccountNonLocked()),
                () -> assertTrue(user.isCredentialsNonExpired()),
                () -> assertTrue(user.isEnabled())
        );


        BDDMockito.verify(userRepository).findById(2L);
        BDDMockito.verifyNoMoreInteractions(userRepository);

    }

    @Test
    void shouldFailWhenRequestAllUsersIfSortPropertyValueNotExists() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "unexpected"));

        BDDMockito.given(this.userRepository.findAll(pageable))
                .willThrow(new PropertyReferenceException("unexpected",
                        TypeInformation.of(User.class), List.of()));

        Assertions.assertThrows(PropertyReferenceException.class,
                () -> this.userService.usersList(pageable));

        BDDMockito.verify(this.userRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
    }

    @Test
    void shouldToReturnAllUsersUnsortedWithSuccess() {
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        List<User> userList = TestsHelper.UserHelper.userList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(userList, pageable, 4)
                        .map(UserSummaryInfo::new);


        BDDMockito.given(this.userRepository.findAll(pageable))
                .willReturn(new PageImpl<>(userList, pageable, 4));


        assertDoesNotThrow(() -> this.userService.usersList(pageable));


        Assertions.assertAll(
                () -> assertEquals(4, userSummaryInfoPage.getContent().size()),
                () -> assertEquals(10, userSummaryInfoPage.getSize()),
                () -> assertEquals(4, userSummaryInfoPage.getTotalElements()),
                () -> assertEquals(1, userSummaryInfoPage.getTotalPages())
        );

        BDDMockito.verify(this.userRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);


    }

    @Test
    void shouldToReturnAllUsersSortedDescendantByIdWithSuccess() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "id"));

        List<User> userList = TestsHelper.UserHelper.userList()
                .stream().sorted(Comparator.comparing(User::getId).reversed())
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(userList, pageable, 4)
                        .map(UserSummaryInfo::new);


        BDDMockito.given(this.userRepository.findAll(pageable))
                .willReturn(new PageImpl<>(userList, pageable, 4));


        assertDoesNotThrow(() -> this.userService.usersList(pageable));


        Assertions.assertAll(
                () -> assertEquals(4L, userSummaryInfoPage.getContent().get(0).id()),
                () -> assertEquals(3L, userSummaryInfoPage.getContent().get(1).id()),
                () -> assertEquals(2L, userSummaryInfoPage.getContent().get(2).id()),
                () -> assertEquals(1L, userSummaryInfoPage.getContent().get(3).id()),
                () -> assertEquals(4, userSummaryInfoPage.getContent().size()),
                () -> assertEquals(10, userSummaryInfoPage.getSize()),
                () -> assertEquals(4, userSummaryInfoPage.getTotalElements()),
                () -> assertEquals(1, userSummaryInfoPage.getTotalPages())
        );

        BDDMockito.verify(this.userRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldToReturnAllUsersSortedAscendantByProfileNameWithSuccess() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "profile.profileName"));

        List<User> userList = TestsHelper.UserHelper.userList()
                .stream().sorted(Comparator.comparing(u -> u.getProfile().getProfileName().name()))
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(userList, pageable, 4)
                        .map(UserSummaryInfo::new);


        BDDMockito.given(this.userRepository.findAll(pageable))
                .willReturn(new PageImpl<>(userList, pageable, 4));


        assertDoesNotThrow(() -> this.userService.usersList(pageable));


        Assertions.assertAll(
                () -> assertEquals(Profile.ProfileName.ADM, userSummaryInfoPage.getContent().get(0).profile().getProfileName()),
                () -> assertEquals(Profile.ProfileName.ANONYMOUS, userSummaryInfoPage.getContent().get(1).profile().getProfileName()),
                () -> assertEquals(Profile.ProfileName.BASIC, userSummaryInfoPage.getContent().get(2).profile().getProfileName()),
                () -> assertEquals(Profile.ProfileName.MOD, userSummaryInfoPage.getContent().get(3).profile().getProfileName()),
                () -> assertEquals(4, userSummaryInfoPage.getContent().size()),
                () -> assertEquals(10, userSummaryInfoPage.getSize()),
                () -> assertEquals(4, userSummaryInfoPage.getTotalElements()),
                () -> assertEquals(1, userSummaryInfoPage.getTotalPages())
        );

        BDDMockito.verify(this.userRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
    }

    @Test
    void shouldToReturnTwoUsersSortedAscendantByFirstNameWithSuccess() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "id"));

        List<User> userList = TestsHelper.UserHelper.userList()
                .stream().filter(user -> user.getFirstName().equals("Joao") ||
                                         user.getFirstName().equals("Jose"))
                .sorted(Comparator.comparing(User::getFirstName))
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(userList, pageable, 4)
                        .map(UserSummaryInfo::new);


        BDDMockito.given(this.userRepository.findAll(pageable))
                .willReturn(new PageImpl<>(userList, pageable, 4));


        assertDoesNotThrow(() -> this.userService.usersList(pageable));


        Assertions.assertAll(
                () -> assertEquals("Joao", userSummaryInfoPage.getContent().get(0).firstName()),
                () -> assertEquals("Jose", userSummaryInfoPage.getContent().get(1).firstName()),
                () -> assertEquals(2, userSummaryInfoPage.getContent().size()),
                () -> assertEquals(10, userSummaryInfoPage.getSize()),
                () -> assertEquals(2, userSummaryInfoPage.getTotalElements()),
                () -> assertEquals(1, userSummaryInfoPage.getTotalPages())
        );

        BDDMockito.verify(this.userRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
    }

    @Test
    void shouldFailToEditUserIfInformedUserNotExists() {
        BDDMockito.given(this.userRepository.findById(5L))
                .willThrow(new InstanceNotFoundException(String.format("Usário [ID: %d] não encontrado", 5L)));

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Marcus", "Silva", "marcus_silva",
                "marcus@email.com", Profile.ProfileName.BASIC, true, true,
                true, true);

        assertThrows(InstanceNotFoundException.class,
                () -> this.userService.updateUser(5L, Profile.ProfileName.BASIC, userUpdateDTO),
                "Usuário [ID: 5] não encontrado");

        BDDMockito.verify(this.userRepository).findById(5L);
        BDDMockito.verify(this.userRepository, never()).save(any(User.class));
        BDDMockito.verifyNoInteractions(this.profileRepository);
        BDDMockito.verifyNoMoreInteractions(this.userRepository);


    }

    @Test
    void shouldFailToEditUserIfInformedProfileNotExists() {
        BDDMockito.given(this.userRepository.findById(2L))
                .willReturn(Optional.ofNullable(TestsHelper.UserHelper.userList().get(1)));

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.BASIC))
                .willThrow(InstanceNotFoundException.class);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Marcus", "Silva", "marcus_silva",
                "marcus@email.com", Profile.ProfileName.BASIC, true, true,
                true, true);

        assertThrows(InstanceNotFoundException.class,
                () -> this.userService.updateUser(2L, Profile.ProfileName.BASIC, userUpdateDTO),
                "Perfil não encontrado");


        BDDMockito.verify(this.userRepository).findById(2L);
        BDDMockito.verify(this.profileRepository).findByProfileName(Profile.ProfileName.BASIC);
        BDDMockito.verify(this.userRepository, never()).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
        BDDMockito.verifyNoMoreInteractions(this.profileRepository);


    }

    @Test
    void shouldNotEditExtraInformationIfEditRequestUserIsNotADM() {
        User user = TestsHelper.UserHelper.userList().get(1);

        BDDMockito.given(this.userRepository.findById(2L)).willReturn(Optional.of(user));

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.ADM))
                .willReturn(Optional.ofNullable(TestsHelper.ProfileHelper.profileList().get(3)));

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Marcus", "Silva", "marcus_silva",
                "marcus@email.com", Profile.ProfileName.ADM, false, true,
                true, false);


        assertAll(
                () -> assertDoesNotThrow(() -> this.userService.updateUser(2L, Profile.ProfileName.BASIC, userUpdateDTO)),
                () -> assertEquals("Jose", user.getFirstName()),
                () -> assertEquals("Silva", user.getLastName()),
                () -> assertEquals("marcus_silva", user.getUsername()),
                () -> assertEquals("marcus@email.com", user.getEmail()),
                () -> assertEquals(Profile.ProfileName.BASIC, user.getProfile().getProfileName()),
                () -> assertTrue(user.isAccountNonExpired()),
                () -> assertTrue(user.isAccountNonLocked()),
                () -> assertTrue(user.isCredentialsNonExpired()),
                () -> assertTrue(user.isEnabled())
        );


        BDDMockito.verify(this.userRepository).findById(2L);
        BDDMockito.verify(this.profileRepository).findByProfileName(Profile.ProfileName.ADM);
        BDDMockito.verify(this.userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
        BDDMockito.verifyNoMoreInteractions(this.profileRepository);


    }


    @Test
    void shouldEditExtraInformationIfEditRequestUserIsADM() {
        User user = TestsHelper.UserHelper.userList().get(1);

        BDDMockito.given(this.userRepository.findById(2L)).willReturn(Optional.of(user));

        BDDMockito.given(this.profileRepository.findByProfileName(Profile.ProfileName.ADM))
                .willReturn(Optional.ofNullable(TestsHelper.ProfileHelper.profileList().get(3)));

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Marcus", "Silva", "marcus_silva",
                "marcus@email.com", Profile.ProfileName.ADM, false, true,
                true, false);

        assertAll(
                () -> assertDoesNotThrow(() -> this.userService.updateUser(2L, Profile.ProfileName.ADM, userUpdateDTO)),
                () -> assertEquals("Marcus", user.getFirstName()),
                () -> assertEquals("Silva", user.getLastName()),
                () -> assertEquals("marcus_silva", user.getUsername()),
                () -> assertEquals("marcus@email.com", user.getEmail()),
                () -> assertEquals(Profile.ProfileName.ADM, user.getProfile().getProfileName()),
                () -> assertFalse(user.isAccountNonExpired()),
                () -> assertTrue(user.isAccountNonLocked()),
                () -> assertTrue(user.isCredentialsNonExpired()),
                () -> assertFalse(user.isEnabled())
        );


        BDDMockito.verify(this.userRepository).findById(2L);
        BDDMockito.verify(this.profileRepository).findByProfileName(Profile.ProfileName.ADM);
        BDDMockito.verify(this.userRepository).save(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.userRepository);
        BDDMockito.verifyNoMoreInteractions(this.profileRepository);

    }

    @Test
    void shouldFailToDeleteUserIfRequestedUserNotExists() {
        BDDMockito.given(this.userRepository.findById(5L))
                .willThrow(new InstanceNotFoundException(String.format("Usário [ID: %d] não encontrado", 5L)));

        assertThrows(InstanceNotFoundException.class, () -> this.userService.getDetailedInfoUser(5L),
                "Usuário [ID: 5] não encontrado");

        BDDMockito.verify(this.userRepository).findById(5L);
        BDDMockito.verify(this.userRepository, never()).delete(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }

    @Test
    void shouldDeleteUserWithSuccessIfRequestedUserExists() {
        BDDMockito.given(this.userRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.UserHelper.userList().get(1)));

        assertDoesNotThrow(() -> this.userService.deleteUser(2L));

        BDDMockito.verify(this.userRepository).findById(2L);
        BDDMockito.verify(this.userRepository).delete(any(User.class));
        BDDMockito.verifyNoMoreInteractions(this.userRepository);

    }


}
