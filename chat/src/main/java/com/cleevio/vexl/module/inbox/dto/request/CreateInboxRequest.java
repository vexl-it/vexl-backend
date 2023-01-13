package com.cleevio.vexl.module.inbox.dto.request;

import com.cleevio.vexl.common.annotation.NullOrNotBlank;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CreateInboxRequest(

        @NotBlank
        @Schema(required = true, description = "Identifier of the inbox. Must be unique.")
        String publicKey,

        @Nullable
        @NullOrNotBlank
        @Schema(description = "Firebase token for notification about new messages.")
        String token,

        @Valid
        @NotNull
        SignedChallenge signedChallenge

) {
}
