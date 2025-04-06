package com.raul.forumhub.topic.client;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("forumhub.client.user")
@Getter
@Setter
public class UserClientProperties {

    @NotNull
    private String scheme;
    @NotNull
    private String host;
    @NotNull
    private String port;
    @NotNull
    private String path;

}
