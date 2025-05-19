package com.raul.forumhub.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Objects;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class UserSecurityConfig {

    private static final String JWT_AUDIENCE = "hub-user";


    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {
                    if (!jwt.getAudience().contains(JWT_AUDIENCE)) {
                        OAuth2Error error = new OAuth2Error("invalid_token", "Audience unidentified!",
                                null);
                        throw new OAuth2AuthenticationException(error);
                    }

                    String roleAuthority = jwt.getClaim("authority");
                    roleAuthority = Objects.isNull(roleAuthority) ? "ANONYMOUS" : roleAuthority;

                    JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
                    Collection<GrantedAuthority> userScopeAuthorities = scopeConverter.convert(jwt);

                    userScopeAuthorities.add(new SimpleGrantedAuthority(roleAuthority));

                    return userScopeAuthorities;

                });

        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
