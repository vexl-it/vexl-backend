package com.cleevio.vexl.module.challenge.dto.response;

import com.cleevio.vexl.common.serializer.ZonedDateTimeToUnixTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

public record ChallengesBatchCreatedResponse(

        List<ChallengePublicKeyResponse> challenges,

        @JsonSerialize(using = ZonedDateTimeToUnixTimeSerializer.class)
        @Schema(description = "Expiration date returned in Unix timestamp milliseconds.")
        ZonedDateTime expiration

) {

    public record ChallengePublicKeyResponse(

            @Schema(description = "Challenge create for this public key.")
            String publicKey,

            @Schema(description = "Challenge what client has to sign with a private key.")
            String challenge

    ) {
    }

}
