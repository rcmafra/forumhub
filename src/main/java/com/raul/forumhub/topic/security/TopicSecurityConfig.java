package com.raul.forumhub.topic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TopicSecurityConfig {

    @Bean
    public SecurityFilterChain topicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }


    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {

                    String userRoleAuthority = jwt.getClaim("authority");

                    if(Objects.isNull(userRoleAuthority)) {
                        userRoleAuthority = "ANONYMOUS";
                    }

                    JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
                    Collection<GrantedAuthority> userScopeAuthorities = scopeConverter.convert(jwt);

                    userScopeAuthorities.add(new SimpleGrantedAuthority(userRoleAuthority));

                    return userScopeAuthorities;


                });

        return converter;
    }


//    @Bean
//    public OAuth2AuthorizationRequestResolver auth2AuthorizationRequest(ClientRegistrationRepository clientRegistrationRepository,
//                                                                        String authorizationRequestBaseUri){
//        return new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
//    }
//
//
//
//    @Bean
//    public OAuth2AuthorizedClientProvider auth2AuthorizedClientProvider(OAuth2AuthorizationContext context){
//        return new AuthorizationCodeOAuth2AuthorizedClientProvider();
//    }


}
