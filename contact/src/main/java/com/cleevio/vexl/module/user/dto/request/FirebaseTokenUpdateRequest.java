package com.cleevio.vexl.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record FirebaseTokenUpdateRequest(

        @NotBlank
        @Schema(required = true, description = "A new Firebase Token to replace the old one.")
        String firebaseToken

) {
}
