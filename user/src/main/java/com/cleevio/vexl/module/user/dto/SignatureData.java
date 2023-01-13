package com.cleevio.vexl.module.user.dto;

import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;

public record SignatureData(

        @NotBlank
        @Nullable
        String hash,

        @NotBlank
        @Nullable
        String signature,

        boolean challengeVerified

) {
}
