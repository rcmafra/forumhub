package com.raul.forumhub.authorization.server.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class RegisteredClientProperties {

    @Value("${REGISTERED_CLIENT_TOPIC_NAME}")
    String topicName;
    @Value("${REGISTERED_CLIENT_TOPIC_CLIENT-ID}")
    String topicClientID;
    @Value("${REGISTERED_CLIENT_TOPIC_PASSWORD}")
    String topicPassword;
    @Value("${REGISTERED_CLIENT_USER_NAME}")
    String userName;
    @Value("${REGISTERED_CLIENT_USER_CLIENT-ID}")
    String userClientID;
    @Value("${REGISTERED_CLIENT_USER_PASSWORD}")
    String userPassword;
}
