package com.cleevio.vexl.module.user.dto.request;

import com.cleevio.vexl.module.user.serializer.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record PhoneConfirmRequest(

        @NotBlank
        @Schema(required = true, description = "Phone must be valid according industry-standard notation pattern specified by ITU-T E.123")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        String phoneNumber

) {
}
