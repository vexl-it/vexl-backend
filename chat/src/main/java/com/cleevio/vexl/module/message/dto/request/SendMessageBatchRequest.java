package com.cleevio.vexl.module.message.dto.request;

import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = """
        User A - senderPublicKey is public key of user who called this request.
        User B - receiverPublicKey is public key of user who will receive the message.
        
        The sender (user A) must:
        1) Get challenge from challenge API.
        2) Sign the challenge with his private key.
        3) Signature send here in signedChallenge.
        4) BE will verify signature, if it valid, then BE will make signed changes and will send all messages to users.
        """)
public record SendMessageBatchRequest(

        @NotEmpty
        @Schema(required = true)
        List<@Valid BatchData> data

) {

    public record BatchData(

            @NotBlank
            @Schema(required = true)
            String senderPublicKey,

            @NotEmpty
            @Schema(required = true)
            List<@Valid Messages> messages,

            @Valid
            @NotNull
            @Schema(required = true)
            SignedChallenge signedChallenge
    ) {

        public record Messages(

                @NotBlank
                @Schema(required = true)
                String receiverPublicKey,

                @NotBlank
                @Schema(required = true)
                String message,

                @NotNull
                @Schema(required = true)
                MessageType messageType
        ) {
        }
    }

}
