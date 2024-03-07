package com.cleevio.vexl.module.message.dto.response;

import com.cleevio.vexl.module.message.constant.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MessagesResponse(

        List<MessageResponse> messages

) {
    public record MessageResponse(

            @Schema(description = "For ordering purposes.")
            Long id,

            @Schema(description = "Encrypted message.")
            String message,

            @Schema(description = "Public key of sender. Reply to this public key.")
            String senderPublicKey,

            @Schema(description = "Type of message.")
            String messageType

    ) {
    }
}
