package com.cleevio.vexl.module.inbox.dto.request;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record BlockInboxRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of inbox which is blocking other inbox.")
        String publicKey,

        @NotBlank
        @Schema(required = true, description = "Public key which will be blocked/unblocked.")
        String publicKeyToBlock,

        @NotNull
        @Schema(required = true, description = "Whether you block (true) or unblock (false) the public key.")
        Boolean block,

        @Valid
        @NotNull
        SignedChallenge signedChallenge

) {
}
