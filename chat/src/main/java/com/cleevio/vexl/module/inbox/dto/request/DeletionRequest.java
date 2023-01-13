package com.cleevio.vexl.module.inbox.dto.request;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record DeletionRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of an Inbox.")
        String publicKey,

        @Valid
        @NotNull
        SignedChallenge signedChallenge

) {
}
