package com.cleevio.vexl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "secret")
public record SecretKeyConfig(

        @NotBlank
        String signaturePublicKey,

        @NotBlank
        String aesKey

) {
}
