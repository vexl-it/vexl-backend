package com.cleevio.vexl.module.message.dto.request;

import com.cleevio.vexl.common.annotation.CheckAllowedMessageType;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record SendMessageRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of an user of an offer who is sending the message.")
        String senderPublicKey,

        @NotBlank
        @Schema(required = true, description = "Public key of an user or an offer to whom the message is to be sent.")
        String receiverPublicKey,

        @NotBlank
        @Schema(required = true, description = "Message to be sent.")
        String message,

        @CheckAllowedMessageType
        @Schema(required = true, description = "Type of message you're sending. Options - MESSAGE, REQUEST_REVEAL, APPROVE_REVEAL, DELETE_CHAT. " +
                "Types - REQUEST_MESSAGING, APPROVE_MESSAGING and DISAPPROVE_MESSAGING are not possible to send here. " +
                "They will be automatically assigned to message sent via permission EPs.")
        MessageType messageType,

        @Valid
        @NotNull
        SignedChallenge signedChallenge
) {
}
