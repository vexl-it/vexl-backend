package com.cleevio.vexl.module.message.service.query;

import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.constant.MessageType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record SendMessageToInboxQuery(

        @NotBlank
        String senderPublicKey,

        // Receiver public key from receiver inbox cannot be used, because public key is in sha256Hash on entity.
        @NotBlank
        String receiverPublicKey,

        @NotNull
        Inbox receiverInbox,

        @NotBlank
        String message,

        @NotNull
        String messageType,

        @Nullable
        String messagePreview
) {
}
