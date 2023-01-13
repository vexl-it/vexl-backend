package com.cleevio.vexl.module.message.dto.request;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record MessageRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of inbox from which client wants to pull the messages")
        String publicKey,


        @Valid
        @NotNull
        SignedChallenge signedChallenge

) {
}
