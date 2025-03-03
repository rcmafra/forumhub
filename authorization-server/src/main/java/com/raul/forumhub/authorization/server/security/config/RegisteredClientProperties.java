package com.raul.forumhub.authorization.server.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class RegisteredClientProperties {

    @Value("${registered.client.topic.name}")
    String topicName;
    @Value("${registered.client.topic.client-id}")
    String topicClientID;
    @Value("${registered.client.topic.password}")
    String topicPassword;
    @Value("${registered.client.topic.topicUrl}")
    String topicUrl;
    @Value("${registered.client.user.name}")
    String userName;
    @Value("${registered.client.user.client-id}")
    String userClientID;
    @Value("${registered.client.user.password}")
    String userPassword;
    @Value("${registered.client.user.userUrl}")
    String userUrl;
}
