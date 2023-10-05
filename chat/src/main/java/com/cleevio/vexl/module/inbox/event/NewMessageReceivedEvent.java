package com.cleevio.vexl.module.inbox.event;

import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.Platform;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record NewMessageReceivedEvent(

        @NotBlank
        String token,

        int clientVersion,

        @NotNull
        Platform platform,

        @NotNull
        MessageType messageType,

        @NotBlank
        String receiverPublicKey,

        @NotBlank
        String senderPublicKey,

        @Nullable
        String messagePreview
) {
}
