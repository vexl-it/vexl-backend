package com.cleevio.vexl.module.challenge.service.query;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record VerifySignedChallengeQuery(

        @NotBlank
        String publicKey,

        @Valid
        @NotNull
        SignedChallenge signedChallenge
) {
}
