package com.cleevio.vexl.common.integration.firebase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(

        @NotBlank
        String clientId,

        @NotBlank
        String clientEmail,

        @NotBlank
        String privateKey,

        @NotBlank
        String privateKeyId,

        @NotBlank
        String projectId,

        @NotBlank
        String tokenUri,

        @NotBlank
        String serviceType

) {
}