package com.raul.forumhub.auth.server.security.config;

import com.raul.forumhub.auth.server.domain.UserEntity;
import com.raul.forumhub.auth.server.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {


    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http.exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                .oauth2ResourceServer((resource) ->
                        resource.jwt(Customizer.withDefaults()));
        return http.build();

    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults()).build();
    }


    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserRepository userRepository) {
        return (context -> {
            Authentication authentication = context.getPrincipal();
            if(authentication.getPrincipal() instanceof User){
                final String email = authentication.getName();
                final UserEntity user = userRepository.findByEmail(email).orElseThrow();

                context.getClaims().claim("sub", email);
                context.getClaims().claim("user_id", user.getId().toString());
                context.getClaims().claim("name", user.getName());
                context.getClaims().claim("authority", "ROLE_" + user.getProfile().getProfileName());
            }
        });
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(BCryptPasswordEncoder bCryptPasswordEncoder,
                                                                 JdbcTemplate jdbcTemplate) {
        RegisteredClient topicApiClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientName("forumhub-topic")
                .clientId("hub-topic")
                .clientSecret(bCryptPasswordEncoder.encode("topic123456"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                .redirectUri("https://oidcdebugger.com/debug")
                .redirectUri("http://localhost:8082/authorized")
                .scopes((scp) -> scp.addAll(Set.of(
                        "topic:delete", "topic:edit",
                        "course:create", "course:delete", "course:edit",
                        "answer:delete", "answer:edit")
                ))
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .authorizationCodeTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .reuseRefreshTokens(false).build())
                .build();

        RegisteredClient userApiClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientName("forumhub-user")
                .clientId("hub-user")
                .clientSecret(bCryptPasswordEncoder.encode("user123456"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                .redirectUri("https://oidcdebugger.com/debug")
                .scopes((scp) -> scp.addAll(Set.of(
                        "myuser:read", "myuser:delete", "myuser:edit", "user:readAll", "user:read")
                ))
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .authorizationCodeTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .reuseRefreshTokens(false).build())
                .build();

        return new InMemoryRegisteredClientRepository(topicApiClient, userApiClient);
    }



//    @Bean
//    public OAuth2AuthorizationService auth2AuthorizationService(JdbcOperations jdbcOperations,
//                                                                RegisteredClientRepository clientRepository){
//        return new JdbcOAuth2AuthorizationService(jdbcOperations, clientRepository);
//    }
//
//
//    @Bean
//    public OAuth2AuthorizationConsentService auth2AuthorizationConsentService(JdbcOperations jdbcOperations,
//                                                                              RegisteredClientRepository clientRepository){
//        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, clientRepository);
//    }


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer("http://127.0.0.1:8082").build();
    }


}


