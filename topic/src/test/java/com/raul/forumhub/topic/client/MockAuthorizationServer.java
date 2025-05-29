package com.raul.forumhub.topic.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;

@TestConfiguration
public class MockAuthorizationServer {

    static RSAKey rsaKey;

    static final String JWT;


    static {
        generateKeyPair();
        JWT = signJwt();

    }


    static final Dispatcher dispatcher = new Dispatcher() {

        @SneakyThrows
        @NotNull
        @Override
        public MockResponse dispatch(@NotNull RecordedRequest request) {
            Assert.notNull(request, "Request cannot be null");
            Assert.notNull(JWT, "Jwt cannot be null");

            final Map<String, Object> wellKnowEndpoints = Map.of(
                    "token_endpoint", "http://127.0.0.1:8092/oauth2/token",
                    "issuer", "http://127.0.0.1:8092",
                    "subject_types_supported", List.of("public"),
                    "jwks_uri", "http://127.0.0.1:8092/oauth2/jwks"

            );

            MockResponse mockResponse = new MockResponse();
            return switch (Objects.requireNonNull(request.getPath())) {
                case "/.well-known/openid-configuration" -> mockResponse
                        .addHeader("Content-Type", "application/json")
                        .setBody(new ObjectMapper().writeValueAsString(wellKnowEndpoints))
                        .setResponseCode(200);
                case "/oauth2/token" -> mockResponse
                        .addHeader("Content-Type", "application/json")
                        .setBody(JWT)
                        .setResponseCode(200);
                default -> new MockResponse().setResponseCode(404);
            };
        }
    };

    private static void generateKeyPair() {
        try {
            rsaKey = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(new Algorithm("RS256"))
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public static String signJwt() {
        try {
            var signer = new RSASSASigner(rsaKey);
            var claimsSet = new JWTClaimsSet.Builder()
                    .subject("hub-user")
                    .claim("scope", "myuser:read")
                    .expirationTime(Date.from(Instant.now().plusSeconds(180L)))
                    .build();
            var signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return writeJwtSignToJson(signedJWT);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private static String writeJwtSignToJson(SignedJWT signer) {
        try {
            return new ObjectMapper().writeValueAsString(
                    Map.of("access_token", signer.serialize(),
                            "scope", signer.getJWTClaimsSet().getStringClaim("scope"),
                            "token_type", "Bearer",
                            "expires_in", signer.getJWTClaimsSet().getExpirationTime()
                    )
            );
        } catch (ParseException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
