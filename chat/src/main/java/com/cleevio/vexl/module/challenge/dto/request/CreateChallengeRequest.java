package com.cleevio.vexl.module.challenge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record CreateChallengeRequest(

        @NotBlank
        @Schema(required = true, description = "Public key for which I want to create a challenge.")
        String publicKey

) {
}
