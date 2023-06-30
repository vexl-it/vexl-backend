package com.cleevio.vexl.module.inbox.dto.request;

import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record LeaveChatRequest(
        @NotBlank
        @Schema(required = true, description = "Public key of an user of an offer who is sending the message.")
        String senderPublicKey,

        @NotBlank
        @Schema(required = true, description = "Public key of an user or an offer to whom the message is to be sent.")
        String receiverPublicKey,
        @Valid
        @NotNull
        SignedChallenge signedChallenge,

        @NotBlank
        @Schema(required = true, description = "Message to be sent.")
        String message

){

}
