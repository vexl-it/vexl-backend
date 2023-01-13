package com.cleevio.vexl.module.user.dto.response;

import com.cleevio.vexl.module.user.dto.SignatureData;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

public record SignatureResponse(

        @Nullable
        @Schema(description = "Hash for phone number/facebookId in Base64 format")
        String hash,

        @Nullable
        @Schema(description = "Signature in Base64 format")
        String signature,

        @Schema(description = "Whether challenge is verified successfully")
        boolean challengeVerified

) {


    public SignatureResponse(SignatureData signatureData) {
        this(
                signatureData.hash(),
                signatureData.signature(),
                signatureData.challengeVerified()
        );
    }
}
