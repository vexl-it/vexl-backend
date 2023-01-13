package com.cleevio.vexl.module.user.dto.response;

import com.cleevio.vexl.module.user.entity.UserVerification;
import com.cleevio.vexl.module.user.serializer.DateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

public record PhoneConfirmResponse(

        @Schema(description = "Verification ID. You will have to send it with code from sms.")
        Long verificationId,

        @Schema(description = "Time when code is expired. Default lifetime is 30 seconds.")
        @Nullable
        @JsonSerialize(using = DateTimeSerializer.class)
        ZonedDateTime expirationAt

) {

    public PhoneConfirmResponse(UserVerification verification) {
        this(
                verification.getId(),
                verification.getExpirationAt()
        );
    }
}
