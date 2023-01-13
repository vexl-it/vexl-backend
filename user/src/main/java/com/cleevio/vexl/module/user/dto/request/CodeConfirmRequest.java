package com.cleevio.vexl.module.user.dto.request;

import com.cleevio.vexl.module.user.serializer.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CodeConfirmRequest(

        @NotNull
        @Schema(required = true, description = "ID of verification. You should get it in phone verification endpoint.")
        Long id,

        @NotBlank
        @Schema(required = true, description = "Code from SMS for verification.")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        String code,

        @NotBlank
        @Schema(required = true, description = "Base64 encoded user's public_key")
        String userPublicKey

) {
}
