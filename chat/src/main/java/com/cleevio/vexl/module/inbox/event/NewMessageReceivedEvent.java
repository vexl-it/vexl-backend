package com.cleevio.vexl.module.inbox.event;

import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.Platform;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record NewMessageReceivedEvent(

        @NotBlank
        String token,

        @NotNull
        Platform platform,

        @NotNull
        MessageType messageType,

        @NotBlank
        String receiverPublicKey,

        @NotBlank
        String senderPublicKey

) {
}
