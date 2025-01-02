package com.raul.forumhub.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.domain.User;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.respository.ProfileRepository;
import com.raul.forumhub.user.respository.UserRepository;
import com.raul.forumhub.user.util.TestsHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles(value = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;


    private static boolean hasBeenInitialized = false;

    @BeforeEach
    void setup() {
        if (!hasBeenInitialized) {
            this.profileRepository.saveAll(TestsHelper.ProfileHelper.profileList());
            this.userRepository.saveAll(TestsHelper.UserHelper.userList());
            hasBeenInitialized = true;
        }
    }

    @Order(1)
    @DisplayName("Should fail with status code 400 if firstName property is sent empty when create user")
    @Test
    void shouldFailIfFirstNamePropertyIsEmptyWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O primeiro nome não pode ser vazio")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(2)
    @DisplayName("Should fail with status code 400 if lastName property is sent empty when create user")
    @Test
    void shouldFailIfLastNamePropertyIsEmptyWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("marcus",
                "", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O sobrenome não pode ser vazio")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(3)
    @DisplayName("Should fail with status code 400 if username property is sent empty when create user")
    @Test
    void shouldFailIfUsernamePropertyIsEmptyWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O username não pode ser vazio")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(4)
    @DisplayName("Should fail with status code 400 if email property is sent empty when create user")
    @Test
    void shouldFailIfEmailPropertyIsEmptyWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O email não pode ser vazio")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(5)
    @DisplayName("Should fail with status code 400 if password property is sent null when create user")
    @Test
    void shouldFailIfPasswordPropertyIsNullWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                null);

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A senha não pode ser null")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(6)
    @DisplayName("Should fail with status code 400 if email property format is invalid when create user")
    @Test
    void shouldFailIfEmailPropertyFormatIsInvalidWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Formato do email inválido")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(7)
    @DisplayName("Should fail if password length is less than 8 characters when create user")
    @Test
    void shouldFailIfPasswordLengthIsLess8CharsWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "PA$swd");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha não deve ter menos de 8 caracteres")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(8)
    @DisplayName("Should fail if password length is larger than 16 characters when create user")
    @Test
    void shouldFailIfPasswordLengthIsLarger16CharsWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "PA$sword_L4arg&r_Th4n_16_Char$s");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha não deve ter mais de 16 caracteres")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(9)
    @DisplayName("Should fail if password hasn't at least one character " +
            "uppercase when create user")
    @Test
    void shouldFailIfPasswordHasNotCharUppercaseWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "p4$sword");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter ao menos 1 letra maiúscula")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(10)
    @DisplayName("Should fail if password hasn't at least one character " +
            "lowercase when create user")
    @Test
    void shouldFailIfPasswordHasNotCharLowercaseWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4$SWORD");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter ao menos 1 letra minúscula")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(11)
    @DisplayName("Should fail if password hasn't at least one digit when create user")
    @Test
    void shouldFailIfPasswordHasNotDigitWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "Pa$sword");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter 1 ou mais dígitos")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(12)
    @DisplayName("Should fail if password hasn't at least one special character when create user")
    @Test
    void shouldFailIfPasswordHasNotSpecialCharWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4ssword");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha deve conter 1 ou mais caracteres especiais")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(13)
    @DisplayName("Should fail if exists five sequence alphabetic in the password " +
            "when create user (e.g.: abcde)")
    @Test
    void shouldFailIfPasswordHasSequenceAlphabeticWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "asdfgS$w0rd");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência 'asdfg' não permitida")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(14)
    @DisplayName("Should fail if exists five sequence numerical in the password " +
            "when create user (e.g.: 12345)")
    @Test
    void shouldFailIfPasswordHasSequenceNumericalWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "45678$Sword");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência numérica '45678' não permitida")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(15)
    @DisplayName("Should fail if exists 'qwerty' sequence in the password " +
            "when create user")
    @Test
    void shouldFailIfPasswordHasSequenceQWERTYWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "qwertyS$0rd");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha contém a sequência 'qwerty' não permitida")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(16)
    @DisplayName("Should fail if exists whitespace in the password " +
            "when create user")
    @Test
    void shouldFailIfPasswordHasWhitespaceWhenCreateUser() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$w ord");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        is("A senha possui espaços em branco não permitidos")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(17)
    @DisplayName("Should fail with status code 404 when create user if basic profile not exists")
    @Transactional
    @Test
    void shouldFailToCreateUserIfBasicProfileNotExists() throws Exception {
        this.userRepository.deleteById(1L);
        this.profileRepository.deleteById(1L);

        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Perfil não encontrado")));

        assertAll(
                () -> assertEquals(2, this.userRepository.findAll().size()),
                () -> assertEquals(2, this.profileRepository.findAll().size())
        );

    }

    @Order(18)
    @DisplayName("Should fail with status code 409 when create user if already exists " +
            "an user with same username")
    @Test
    void shouldFailToCreateUserIfAlreadyExistsAnUserWithSameUsername() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "jose_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Payload conflitante")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(19)
    @DisplayName("Should fail with status code 409 when create user if already exists " +
            "an user with same email")
    @Test
    void shouldFailToCreateUserIfAlreadyExistsAnUserWithSameEmail() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "jose@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Payload conflitante")));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }


    @Order(20)
    @DisplayName("Should create user with success if previous premisses are adequate")
    @Test
    void shouldCreateUserWithSuccessIfEverythingIsOk() throws Exception {
        final UserCreateDTO userCreateDTO = new UserCreateDTO("Marcus",
                "Silva", "marcus_silva", "marcus@email.com",
                "P4s$word");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(21)
    @DisplayName("Should fail with status code 401 when to request detailed info " +
            "user if user unauthenticated")
    @Test
    void shouldFailToRequestDetailedInfoUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(22)
    @DisplayName("Should fail with status code 403 when to request detailed info " +
            "user if authenticated user isn't ADM or MOD, or hasn't authority 'myuser:read'")
    @Test
    void shouldFailToRequestDetailedInfoUserIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(23)
    @DisplayName("Should fail with status code 404 when to request detailed info " +
            "user if user authenticated and has suitable authority, but user requested not exists")
    @Test
    void shouldFailToRequestDetailedInfoUserIfHimNotExists() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
                        .queryParam("user_id", "5")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Usuário não encontrado")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(24)
    @DisplayName("BASIC user should be able get detailed info your user with success if " +
            "has authority 'myuser:read' and user_id param is null")
    @Test
    void basicUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(25)
    @DisplayName("MOD user should be able get detailed info your user with success if " +
            "user_id param is null")
    @Test
    void modUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(26)
    @DisplayName("ADM user should be able get detailed info your user with success if " +
            "user_id param is null")
    @Test
    void admUserShouldGetDetailedInfoYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("ADM")));


        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(27)
    @DisplayName("MOD user should be able get detailed info of other user with success if " +
            "user_id param isn't null")
    @Test
    void modUserShouldGetDetailedInfoOfOtherUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(28)
    @DisplayName("ADM user should be able get detailed info of other user with success if " +
            "user_id param isn't null")
    @Test
    void admUserShouldGetDetailedInfoOfOtherUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }


    @Order(29)
    @DisplayName("Should raise exception if BASIC user to request detailed info of other user " +
            "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserToRequestDetailedInfoWithUserIdParamNotNull() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/detailed-info")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(30)
    @DisplayName("Should fail with status code 401 when to request summary info " +
            "user if user unauthenticated")
    @Test
    void shouldFailToRequestSummaryInfoUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/summary-info")
                        .queryParam("user_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(31)
    @DisplayName("Authenticated user should be able of to request user summary info with success")
    @Test
    void AuthenticatedUserShouldToRequestSummaryInfoUserWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/summary-info")
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

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }


    @Order(32)
    @DisplayName("Should fail with status code 401 when to request all " +
            "users if user is unauthenticated")
    @Test
    void shouldFailToRequestAllUsersIfUserIsUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(33)
    @DisplayName("Should fail with status code 403 when to request all users " +
            "if authenticated user isn't ADM or MOD or hasn't authority 'user:readAll'")
    @Test
    void shouldFailToRequestAllUsersIfUserHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(34)
    @DisplayName("Should raise exception if BASIC user to request all users" +
            "same with authority 'user:readAll'")
    @Test
    void shouldFailIfBasicUserToRequestAllUsers() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_BASIC"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(35)
    @DisplayName("MOD user should be able to request all users unsorted with success")
    @Test
    void modUserShouldToRequestAllUsersUnsortedWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MOD"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(36)
    @DisplayName("ADM user should be able to request all users unsorted with success")
    @Test
    void admUserShouldToRequestAllUsersUnsortedWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADM"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(37)
    @DisplayName("MOD user should be able to request all users sorted descendant by id " +
            "with success")
    @Test
    void modUserShouldToRequestAllUsersSortedByIdWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
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
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 6)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[3].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(38)
    @DisplayName("ADM user should be able to request all users sorted descendant by id " +
            "with success")
    @Test
    void admUserShouldToRequestAllUsersSortedByIdWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
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
                .andExpect(jsonPath("$..userSummaryInfoList[0].[?(@.id == 6)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[3].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(39)
    @DisplayName("MOD user should be able to request all users sorted ascendant by profile " +
            "with success")
    @Test
    void modUserShouldToRequestAllUsersSortedByProfileWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .queryParam("sort", "profile.profileName,asc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MOD"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].profile[?(@.profileName == \"ADM\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].profile[?(@.profileName == \"BASIC\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].profile[?(@.profileName == \"BASIC\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[3].profile[?(@.profileName == \"MOD\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(40)
    @DisplayName("ADM user should be able to request all users sorted ascendant by profile " +
            "with success")
    @Test
    void admUserShouldToRequestAllUsersSortedByProfileWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
                        .queryParam("sort", "profile.profileName,asc")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADM"),
                                        new SimpleGrantedAuthority("SCOPE_user:readAll")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..userSummaryInfoList[0].profile[?(@.profileName == \"ADM\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[1].profile[?(@.profileName == \"BASIC\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[2].profile[?(@.profileName == \"BASIC\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList[3].profile[?(@.profileName == \"MOD\")])").exists())
                .andExpect(jsonPath("$..userSummaryInfoList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(41)
    @DisplayName("MOD user should be able to request two users sorted ascendant by firstName " +
            "with success")
    @Test
    void modUserShouldToRequestTwoUsersSortedByFirstNameWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
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
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 2)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(42)
    @DisplayName("ADM user should be able to request two users sorted descendant by firstName " +
            "with success")
    @Test
    void admUserShouldToRequestTwoUsersSortedByFirstNameWithSuccess() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/users/listAll")
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
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 2)]").exists());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(43)
    @DisplayName("Should fail with status code 401 when to edit user " +
            "if user is unauthenticated")
    @Test
    void shouldFailToEditUserIfUnauthenticated() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newJose", "new_jose@email.com",
                Profile.ProfileName.BASIC, true, true,
                true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );
    }

    @Order(44)
    @DisplayName("Should fail with status code 403 when to edit user " +
            "if authenticated user isn't ADM or hasn't authority 'myuser:edit'")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditUser() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newJose", "new_jose@email.com",
                Profile.ProfileName.BASIC, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "1")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );

    }

    @Order(45)
    @DisplayName("Should fail with status code 404 when ADM user request edit " +
            "of other user and this one not exists")
    @Test
    void shouldFailToEditUserIfInformedUserNotExists() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("maria_silva", "maria@email.com",
                Profile.ProfileName.MOD, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .queryParam("user_id", "5")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Usuário não encontrado")));

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(46)
    @DisplayName("If user isn't ADM shouldn't edit extra information, i.e, profile, expired account, " +
            "locked account, expired credentials and enabled account")
    @Test
    void shouldNotEditExtraInformationIfEditRequestUserIsNotADM() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("jose_silva0", "silva@email.com",
                Profile.ProfileName.ADM, false, true,
                false, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username", is("jose_silva0")))
                .andExpect(jsonPath("$.user.email", is("silva@email.com")))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")))
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)));


        User user = this.userRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("jose_silva0")),
                () -> assertThat(user.getEmail(), is("silva@email.com"))
        );


    }

    @Order(47)
    @DisplayName("BASIC user should be able of edit your user with success if " +
            "user_id param is null and has authority 'myuser:edit'")
    @Test
    void basicUserShouldEditYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newJose", "new_jose@email.com",
                Profile.ProfileName.BASIC, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("BASIC")));

        User user = this.userRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("newJose")),
                () -> assertThat(user.getEmail(), is("new_jose@email.com"))
        );

    }

    @Order(48)
    @DisplayName("MOD user should be able of edit your user with success if " +
            "user_id param is null and has authority 'myuser:edit'")
    @Test
    void modUserShouldEditYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newMaria", "new_maria@email.com",
                Profile.ProfileName.MOD, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")));


        User user = this.userRepository.findById(2L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("newMaria")),
                () -> assertThat(user.getEmail(), is("new_maria@email.com"))
        );

    }

    @Order(49)
    @DisplayName("ADM user should be able of edit your user with success if " +
            "user_id param is null")
    @Test
    void admUserShouldEditYourUserWithSuccessIfUserIdParamIsNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newJoao", "new_joao@email.com",
                Profile.ProfileName.ADM, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("ADM")));


        User user = this.userRepository.findById(3L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("newJoao")),
                () -> assertThat(user.getEmail(), is("new_joao@email.com"))
        );

    }


    @Order(50)
    @DisplayName("ADM user should be able of edit other user with success if " +
            "user_id param isn't null")
    @Test
    void admUserShouldEditOtherUserWithSuccessIfUserIdParamIsNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("maria_silva", "maria@email.com",
                Profile.ProfileName.MOD, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
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
                .andExpect(jsonPath("$.user.accountNonExpired", is(true)))
                .andExpect(jsonPath("$.user.accountNonLocked", is(true)))
                .andExpect(jsonPath("$.user.credentialsNonExpired", is(true)))
                .andExpect(jsonPath("$.user.enabled", is(true)))
                .andExpect(jsonPath("$.user.profile.profileName", is("MOD")));


        User user = this.userRepository.findById(2L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("maria_silva")),
                () -> assertThat(user.getEmail(), is("maria@email.com"))
        );

    }

    @Order(51)
    @DisplayName("Should raise exception if BASIC user to try edit other user " +
            "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserTryEditWithUserIdParamNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("newMaria", "new_maria@email.com",
                Profile.ProfileName.MOD, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .queryParam("user_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());


        User user = this.userRepository.findById(2L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("maria_silva")),
                () -> assertThat(user.getEmail(), is("maria@email.com"))
        );

    }

    @Order(52)
    @DisplayName("Should raise exception if MOD user to try edit other user " +
            "or yourself with user_id param not null")
    @Test
    void shouldFailIfModUserTryEditWithUserIdParamNotNull() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO("jose_silva", "jose@email.com",
                Profile.ProfileName.BASIC, true, true, true, true);

        this.mockMvc.perform(put("/api-forum/v1/forumhub/users/update")
                        .queryParam("user_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:edit")))
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());


        User user = this.userRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertThat(user.getUsername(), is("newJose")),
                () -> assertThat(user.getEmail(), is("new_jose@email.com"))
        );
    }

    @Order(53)
    @DisplayName("Should fail with status code 401 when to delete user " +
            "if user is unauthenticated")
    @Test
    void shouldFailToDeleteUserIfUnauthenticated() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size())
        );


    }

    @Order(54)
    @DisplayName("Should fail with status code 403 when to delete user " +
            "if authenticated user isn't ADM or hasn't authority 'myuser:delete'")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteUser() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(1L).isPresent())
        );


    }

    @Order(55)
    @DisplayName("Should raise exception if BASIC user to try delete other user " +
            "or yourself with user_id param not null")
    @Test
    void shouldFailIfBasicUserTryToDeleteWithUserIdParamNotNull() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .queryParam("user_id", "4")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(6L).isPresent())
        );
    }

    @Order(56)
    @DisplayName("Should raise exception if MOD user to try delete other user " +
            "or yourself with user_id param not null")
    @Test
    void shouldFailIfModUserTryToDeleteWithUserIdParamNotNull() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/update")
                        .queryParam("user_id", "6")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        assertAll(
                () -> assertEquals(4, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(6L).isPresent())
        );
    }


    @Order(57)
    @DisplayName("ADM user should be able of delete other user with success if " +
            "user_id param isn't null")
    @Test
    void admUserShouldDeleteOtherUserWithSuccessIfUserIdParamIsNotNull() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .queryParam("user_id", "6")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        assertAll(
                () -> assertEquals(3, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(6L).isEmpty())
        );

    }

    @Order(58)
    @DisplayName("BASIC user should be able of delete your user with success if " +
            "user_id param is null and has authority 'myuser:delete'")
    @Test
    void basicUserShouldDeleteYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "1",
                                        "authority", "ROLE_BASIC"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        assertAll(
                () -> assertEquals(2, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(1L).isEmpty())
        );

    }

    @Order(59)
    @DisplayName("MOD user should be able of delete your user with success if " +
            "user_id param is null and has authority 'myuser:delete'")
    @Test
    void modUserShouldDeleteYourUserWithSuccessIfHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "2",
                                        "authority", "ROLE_MOD"))))
                                .authorities(new SimpleGrantedAuthority("SCOPE_myuser:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        assertAll(
                () -> assertEquals(1, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(2L).isEmpty())
        );

    }

    @Order(60)
    @DisplayName("ADM user should be able of delete your user with success if " +
            "user_id param is null")
    @Test
    void admUserShouldDeleteYourUserWithSuccessIfUserIdParamIsNull() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/users/delete")
                        .with(jwt().jwt(jwt -> jwt.claims(map -> map.putAll(Map.of(
                                        "user_id", "3",
                                        "authority", "ROLE_ADM"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));


        assertAll(
                () -> assertEquals(0, this.userRepository.findAll().size()),
                () -> assertEquals(3, this.profileRepository.findAll().size()),
                () -> assertTrue(this.userRepository.findById(3L).isEmpty())
        );

    }


}
