package com.raul.forumhub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.UserDetailedInfo;
import com.raul.forumhub.user.dto.response.UserSummaryInfo;
import com.raul.forumhub.user.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.user.security.UserSecurityConfig;
import com.raul.forumhub.user.service.UserService;
import com.raul.forumhub.user.util.TestsHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {UserController.class, UserSecurityConfig.class,
        GlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;


    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isNotFound());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail if password length is less than 8 characters when create user")
    @Test
    void shouldFailIfPasswordLengthIsLess8CharsWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "PA$swd");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha não deve ter menos de 8 caracteres")));

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail if password length is larger than 16 characters when create user")
    @Test
    void shouldFailIfPasswordLengthIsLarger16CharsWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "PA$sword_L4arg&r_Th4n_16_Char$s");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha não deve ter mais de 16 caracteres")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if password hasn't at least one character " +
                 "uppercase when create user")
    @Test
    void shouldFailIfPasswordHasNotCharUppercaseWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "p4$sword");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter ao menos 1 letra maiúscula")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if password hasn't at least one character " +
                 "lowercase when create user")
    @Test
    void shouldFailIfPasswordHasNotCharLowercaseWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4$SWORD");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter ao menos 1 letra minúscula")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if password hasn't at least one digit when create user")
    @Test
    void shouldFailIfPasswordHasNotDigitWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "Pa$sword");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter 1 ou mais dígitos")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if password hasn't at least one special character when create user")
    @Test
    void shouldFailIfPasswordHasNotSpecialCharWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4ssword");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter 1 ou mais caracteres especiais")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if exists five sequence alphabetic in the password " +
                 "when create user (e.g.: abcde)")
    @Test
    void shouldFailIfPasswordHasSequenceAlphabeticWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "asdfgS$w0rd");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência 'asdfg' não permitida")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if exists five sequence numerical in the password " +
                 "when create user (e.g.: 12345)")
    @Test
    void shouldFailIfPasswordHasSequenceNumericalWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "45678$Sword");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência numérica '45678' não permitida")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if exists 'qwerty' sequence in the password " +
                 "when create user")
    @Test
    void shouldFailIfPasswordHasSequenceQWERTYWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "qwertyS$0rd");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência 'qwerty' não permitida")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should fail if exists whitespace in the password " +
                 "when create user")
    @Test
    void shouldFailIfPasswordHasWhitespaceWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$w ord");

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha possui espaços em branco não permitidos")));

        BDDMockito.verifyNoInteractions(this.userService);


    }

    @DisplayName("Should create user with success if every thing is ok")
    @Test
    void shouldCreateUserWithSuccessIfEveryThingIsOk() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Jose", "Silva",
                "jose_silva", "jose_silva@email.com", "P4s$word");

        BDDMockito.given(this.userService.registerUser(userCreateDTO))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(post("/forumhub.io/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated());

        BDDMockito.verify(this.userService).registerUser(userCreateDTO);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 401 when to request detailed info " +
                 "user if user unauthenticated")
    @Test
    void shouldFailToRequestDetailedInfoUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 403 when to request detailed info " +
                 "user if authenticated user isn't ADM or MOD, or hasn't authority 'myuser:read'")
    @Test
    void shouldFailToRequestDetailedInfoUserIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 when request detailed info user" +
                 " with param different of type number, if him exists")
    @Test
    void shouldFailToRequestDetailedInfoUserIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .queryParam("user_id", "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);
    }

    @DisplayName("BASIC user should be able get detailed info your user with success if " +
                 "has authority 'myuser:read' and user_id param is null")
    @Test
    void basicUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(1L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.firstName", is("Jose")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("jose_silva")))
                .andExpect(jsonPath("$.user.email", is("jose@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).getDetailedInfoUser(1L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("MOD user should be able get detailed info your user with success if " +
                 "user_id param is null")
    @Test
    void modUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(2L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(1)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_MOD")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(2)))
                .andExpect(jsonPath("$.user.firstName", is("Maria")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("maria_silva")))
                .andExpect(jsonPath("$.user.email", is("maria@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).getDetailedInfoUser(2L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("ADM user should be able get detailed info your user with success if " +
                 "user_id param is null")
    @Test
    void admUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(3L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(2)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(3)))
                .andExpect(jsonPath("$.user.firstName", is("Joao")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("joao_silva")))
                .andExpect(jsonPath("$.user.email", is("joao@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("ADM")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).getDetailedInfoUser(3L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("MOD user should be able get detailed info of other user with success if " +
                 "user_id param isn't null")
    @Test
    void modUserShouldGetDetailedInfoOfOtherUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(1L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_MOD")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.firstName", is("Jose")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("jose_silva")))
                .andExpect(jsonPath("$.user.email", is("jose@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).getDetailedInfoUser(1L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("ADM user should be able get detailed info of other user with success if " +
                 "user_id param isn't null")
    @Test
    void admUserShouldGetDetailedInfoOfOtherUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(1L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.firstName", is("Jose")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("jose_silva")))
                .andExpect(jsonPath("$.user.email", is("jose@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).getDetailedInfoUser(1L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("Should raise exception if BASIC user to request detailed info of other user " +
                 "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserToRequestDetailedInfoWithUserIdParamNotNull() throws Exception {
        BDDMockito.given(this.userService.getDetailedInfoUser(1L))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/detailed-info")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should fail with status code 401 when to request summary info " +
                 "user if user unauthenticated")
    @Test
    void shouldFailToRequestSummaryInfoUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/summary-info")
                        .queryParam("user_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Authenticated user should be able of to request user summary info with success")
    @Test
    void AuthenticatedUserShouldToRequestSummaryInfoUserWithSuccess() throws Exception {
        BDDMockito.given(this.userService.getUserById(1L))
                .willReturn(TestsHelper.UserHelper.userList().get(0));

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/summary-info")
                        .queryParam("user_id", "1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Jose")))
                .andExpect(jsonPath("$.lastName", is("Silva")))
                .andExpect(jsonPath("$.username", is("jose_silva")))
                .andExpect(jsonPath("$.email", is("jose@email.com")))
                .andExpect(jsonPath("$.profile.profileName", is("BASIC")));

        BDDMockito.verify(this.userService).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 if request user summary info " +
                 "with param different of type number, if him exists")
    @Test
    void shouldFailToRequestSummaryInfoUserIfParamDifferentOfNumber() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/summary-info")
                        .queryParam("user_id", "unexpected")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should fail with status code 401 when to request all " +
                 "users if user is unauthenticated")
    @Test
    void shouldFailToRequestAllUsersIfUserIsUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should fail with status code 403 when to request all users " +
                 "if authenticated user isn't ADM or MOD or hasn't authority 'user:readAll'")
    @Test
    void shouldFailToRequestAllUsersIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should raise exception if BASIC user to request all users" +
                 "same with authority 'user:readAll'")
    @Test
    void shouldFailIfBasicUserToRequestAllUsers() throws Exception {
        Page<UserSummaryInfo> userDetailedInfoPage =
                new PageImpl<>(TestsHelper.UserHelper.userList(),
                        Pageable.unpaged(), 3)
                        .map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(any(Pageable.class)))
                .willReturn(userDetailedInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_BASIC"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.userService);


    }


    @DisplayName("MOD user should be able to request all users unsorted with success")
    @Test
    void modUserShouldToRequestAllUsersUnsortedWithSuccess() throws Exception {
        Page<UserSummaryInfo> userDetailedInfoPage =
                new PageImpl<>(TestsHelper.UserHelper.userList(),
                        Pageable.unpaged(), 3)
                        .map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(any(Pageable.class)))
                .willReturn(userDetailedInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MOD"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(any(Pageable.class));
        BDDMockito.verifyNoMoreInteractions(this.userService);


    }


    @DisplayName("ADM user should be able to request all users unsorted with success")
    @Test
    void admUserShouldToRequestAllUsersUnsortedWithSuccess() throws Exception {
        Page<UserSummaryInfo> userDetailedInfoPage =
                new PageImpl<>(TestsHelper.UserHelper.userList(),
                        Pageable.unpaged(), 3)
                        .map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(any(Pageable.class)))
                .willReturn(userDetailedInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADM"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(any(Pageable.class));
        BDDMockito.verifyNoMoreInteractions(this.userService);


    }


    @DisplayName("MOD user should be able to request all users sorted descendant by id " +
                 "with success")
    @Test
    void modUserShouldToRequestAllUsersSortedByIdWithSuccess() throws Exception {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "id"));

        List<User> sortedUserById = TestsHelper.UserHelper.userList()
                .stream().sorted(Comparator.comparing(User::getId).reversed())
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(sortedUserById, pageable, 3).map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(pageable))
                .willReturn(userSummaryInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .queryParam("sort", "id,desc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MOD"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userService);


    }


    @DisplayName("ADM user should be able to request all users sorted descendant by id " +
                 "with success")
    @Test
    void admUserShouldToRequestAllUsersSortedByIdWithSuccess() throws Exception {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "id"));

        List<User> sortedUserById = TestsHelper.UserHelper.userList()
                .stream().sorted(Comparator.comparing(User::getId).reversed())
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(sortedUserById, pageable, 3).map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(pageable))
                .willReturn(userSummaryInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .queryParam("sort", "id,desc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADM"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userService);


    }


    @DisplayName("MOD user should be able to request two users sorted ascendant by firstName " +
                 "with success")
    @Test
    void modUserShouldToRequestTwoUsersSortedByFirstNameWithSuccess() throws Exception {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "firstName"));

        List<User> sortedUserByFirstName = TestsHelper.UserHelper.userList()
                .stream().filter(user -> user.getId() == 1 || user.getId() == 3)
                .sorted(Comparator.comparing(User::getFirstName))
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(sortedUserByFirstName, pageable, 2).map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(pageable))
                .willReturn(userSummaryInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .queryParam("size", "2")
                        .queryParam("sort", "firstName,asc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MOD"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userService);
    }


    @DisplayName("ADM user should be able to request two users sorted descendant by firstName " +
                 "with success")
    @Test
    void admUserShouldToRequestTwoUsersSortedByFirstNameWithSuccess() throws Exception {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "firstName"));

        List<User> sortedUserByFirstName = TestsHelper.UserHelper.userList()
                .stream().filter(user -> user.getId() == 1 || user.getId() == 3)
                .sorted(Comparator.comparing(User::getFirstName))
                .toList();

        Page<UserSummaryInfo> userSummaryInfoPage =
                new PageImpl<>(sortedUserByFirstName, pageable, 2).map(UserSummaryInfo::new);

        BDDMockito.given(this.userService.usersList(pageable))
                .willReturn(userSummaryInfoPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/users/listAll")
                        .queryParam("size", "2")
                        .queryParam("sort", "firstName,asc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADM"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.userService).usersList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.userService);


    }


    @DisplayName("Should fail with status code 401 when to edit user " +
                 "if user is unauthenticated")
    @Test
    void shouldFailToEditUserIfUnauthenticated() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Jose", "Silva", "newJose",
                "new_jose@email.com", Profile.ProfileName.BASIC,
                true, true, true, true);

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should fail with status code 403 when to edit user " +
                 "if authenticated user isn't ADM or hasn't authority 'myuser:edit'")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditUser() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Jose", "Silva", "newJose",
                "new_jose@email.com", Profile.ProfileName.BASIC,
                true, true, true, true);

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "1")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 when ADM user edit other user" +
                 " with param different of type number")
    @Test
    void shouldFailToEditUserIfParamDifferentOfTypeNumber() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Jose", "Silva", "newJose",
                "new_jose@email.com", Profile.ProfileName.BASIC, true,
                true, true, true);

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .queryParam("user_id", "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 when edit user if profile sent is" +
                 "different of the enum types available")
    @Test
    void shouldFailToEditUserIfEnumTypeSentNonExists() throws Exception {
        String request = """
                {
                 "username": "newJose",
                 "profile": "unexpected",
                 "email": "new_jose@email.com",
                 "accountNonExpired",: true,
                 "accountNonLocked": true,
                 "credentialsNonExpired": true,
                 "enabled": true
                }
                """;

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("BASIC user should be able of edit your user with success if " +
                 "user_id param is null and has authority 'myuser:edit'")
    @Test
    void basicUserShouldEditYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Jose", "Silva", "newJose",
                "new_jose@email.com", Profile.ProfileName.BASIC,
                true, true, true, true);

        User user = TestsHelper.UserHelper.userList().get(0);
        user.setUsername("newJose");
        user.setEmail("new_jose@email.com");

        BDDMockito.given(this.userService.updateUser(1L, Profile.ProfileName.BASIC, userUpdateDTO))
                .willReturn(new UserDetailedInfo(user));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.firstName", is("Jose")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("newJose")))
                .andExpect(jsonPath("$.user.email", is("new_jose@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).updateUser(1L, Profile.ProfileName.BASIC, userUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("MOD user should be able of edit your user with success if " +
                 "user_id param is null and has authority 'myuser:edit'")
    @Test
    void modUserShouldEditYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Maria", "Silva", "newMaria",
                "new_maria@email.com", Profile.ProfileName.MOD,
                true, true, true, true);

        User user = TestsHelper.UserHelper.userList().get(1);
        user.setUsername("newMaria");
        user.setEmail("new_maria@email.com");

        BDDMockito.given(this.userService.updateUser(2L, Profile.ProfileName.MOD, userUpdateDTO))
                .willReturn(new UserDetailedInfo(user));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(2)))
                .andExpect(jsonPath("$.user.firstName", is("Maria")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("newMaria")))
                .andExpect(jsonPath("$.user.email", is("new_maria@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).updateUser(2L, Profile.ProfileName.MOD, userUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("ADM user should be able of edit your user with success if " +
                 "user_id param is null")
    @Test
    void admUserShouldEditYourUserWithSuccessIfUserIdParamIsNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Joao", "Silva", "newJoao",
                "new_joao@email.com", Profile.ProfileName.ADM,
                true, true, true, true);

        User user = TestsHelper.UserHelper.userList().get(2);
        user.setUsername("newJoao");
        user.setEmail("new_joao@email.com");

        BDDMockito.given(this.userService.updateUser(3L, Profile.ProfileName.ADM, userUpdateDTO))
                .willReturn(new UserDetailedInfo(user));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(3)))
                .andExpect(jsonPath("$.user.firstName", is("Joao")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("newJoao")))
                .andExpect(jsonPath("$.user.email", is("new_joao@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("ADM")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).updateUser(3L, Profile.ProfileName.ADM, userUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("ADM user should be able of edit other user with success if " +
                 "user_id param isn't null")
    @Test
    void admUserShouldEditOtherUserWithSuccessIfUserIdParamIsNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Maria", "Silva", "maria_silva",
                "maria@email.com", Profile.ProfileName.MOD, true, true,
                true, true);

        User user = TestsHelper.UserHelper.userList().get(1);
        user.setUsername("maria_silva");
        user.setEmail("maria@email.com");

        BDDMockito.given(this.userService.updateUser(2L, Profile.ProfileName.ADM, userUpdateDTO))
                .willReturn(new UserDetailedInfo(user));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(2)))
                .andExpect(jsonPath("$.user.firstName", is("Maria")))
                .andExpect(jsonPath("$.user.lastName", is("Silva")))
                .andExpect(jsonPath("$.user.username", is("maria_silva")))
                .andExpect(jsonPath("$.user.email", is("maria@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        BDDMockito.verify(this.userService).updateUser(2L, Profile.ProfileName.ADM, userUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("Should raise exception if BASIC user to try edit other user " +
                 "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserTryEditWithUserIdParamNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Maria", "Silva", "newMaria",
                "new_maria@email.com", Profile.ProfileName.MOD, true, true,
                true, true);

        BDDMockito.given(this.userService.updateUser(2L, Profile.ProfileName.BASIC, userUpdateDTO))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(1)));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);
    }


    @DisplayName("Should raise exception if MOD user to try edit other user " +
                 "or yourself with user_id param not null")
    @Test
    void shouldFailIfModUserTryEditWithUserIdParamNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("Jose", "Silva", "newJose",
                "new_jose@email.com", Profile.ProfileName.BASIC, true, true,
                true, true);

        BDDMockito.given(this.userService.updateUser(1L, Profile.ProfileName.MOD, userUpdateDTO))
                .willReturn(new UserDetailedInfo(TestsHelper.UserHelper.userList().get(0)));

        this.mockMvc.perform(put("/forumhub.io/api/v1/users/edit")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);
    }


    @DisplayName("Should fail with status code 401 when to delete user " +
                 "if user is unauthenticated")
    @Test
    void shouldFailToDeleteUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("Should fail with status code 403 when to delete user " +
                 "if authenticated user isn't ADM or hasn't authority 'myuser:delete'")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteUser() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.userService);

    }

    @DisplayName("Should fail with status code 400 when ADM user delete other user" +
                 " with param different of type number, if him exists")
    @Test
    void shouldFailIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .queryParam("user_id", "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);

    }


    @DisplayName("BASIC user should be able of delete your user with success if " +
                 "user_id param is null and has authority 'myuser:delete'")
    @Test
    void basicUserShouldDeleteYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(1L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));


        BDDMockito.verify(this.userService).deleteUser(1L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("MOD user should be able of delete your user with success if " +
                 "user_id param is null and has authority 'myuser:delete'")
    @Test
    void modUserShouldDeleteYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(2L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));


        BDDMockito.verify(this.userService).deleteUser(2L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("ADM user should be able of delete your user with success if " +
                 "user_id param is null")
    @Test
    void admUserShouldDeleteYourUserWithSuccessIfUserIdParamIsNull() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(3L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));


        BDDMockito.verify(this.userService).deleteUser(3L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("ADM user should be able of delete other user with success if " +
                 "user_id param isn't null")
    @Test
    void admUserShouldDeleteOtherUserWithSuccessIfUserIdParamIsNotNull() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(3L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));


        BDDMockito.verify(this.userService).deleteUser(2L);
        BDDMockito.verifyNoMoreInteractions(this.userService);

    }


    @DisplayName("Should raise exception if BASIC user to try delete other user " +
                 "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserTryToDeleteWithUserIdParamNotNull() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(2L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);
    }


    @DisplayName("Should raise exception if MOD user to try delete other user " +
                 "or yourself with user_id param not null")
    @Test
    void shouldFailIfModUserTryToDeleteWithUserIdParamNotNull() throws Exception {
        BDDMockito.doNothing().when(this.userService).deleteUser(1L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/users/delete")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userService);
    }


}
