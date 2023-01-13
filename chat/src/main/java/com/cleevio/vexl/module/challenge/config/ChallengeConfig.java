package com.cleevio.vexl.module.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "challenge")
public record ChallengeConfig(

        @Positive
        int expiration //minutes

) {
}
