package com.cleevio.vexl.module.user.dto.request;

import com.cleevio.vexl.common.annotation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

public record CreateUserRequest(

        @Nullable
        @NullOrNotBlank
        @Schema(description = "Firebase token for the push notifications.")
        String firebaseToken

) {
}
