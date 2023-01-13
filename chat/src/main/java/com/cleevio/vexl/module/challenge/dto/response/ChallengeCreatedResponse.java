package com.cleevio.vexl.module.challenge.dto.response;

import com.cleevio.vexl.common.serializer.ZonedDateTimeToUnixTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

public record ChallengeCreatedResponse(

        @Schema(description = "Challenge what client has to sign with a private key.")
        String challenge,

        @JsonSerialize(using = ZonedDateTimeToUnixTimeSerializer.class)
        @Schema(description = "Expiration date returned in Unix timestamp milliseconds.")
        ZonedDateTime expiration

) {
}
