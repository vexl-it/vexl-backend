package com.cleevio.vexl.module.user.dto.response;

import com.cleevio.vexl.module.user.entity.UserVerification;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

public record ConfirmCodeResponse(

        @Nullable
        @Schema(description = "Challenge for user. It is used to verify that the public key is really his.")
        String challenge,

        @Schema(description = "Boolean whether is phone verified.")
        boolean phoneVerified

) {
    public ConfirmCodeResponse(UserVerification challengeVerification) {
        this(
                challengeVerification.getChallenge(),
                challengeVerification.isPhoneVerified()
        );
    }
}
