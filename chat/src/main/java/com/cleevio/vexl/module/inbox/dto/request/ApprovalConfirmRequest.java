package com.cleevio.vexl.module.inbox.dto.request;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record ApprovalConfirmRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of inbox which confirms the sender.")
        String publicKey,

        @NotBlank
        @Schema(required = true, description = "Public key of inbox you want to confirm.")
        String publicKeyToConfirm,

        @NotBlank
        @Schema(required = true, description = "Confirmation message.")
        String message,

        @NotNull
        @Schema(required = true, description = "If you want to approve user, send 'true'. If you want to disapprove user, send 'false'")
        Boolean approve,

        @Valid
        @NotNull
        SignedChallenge signedChallenge

) {
}
