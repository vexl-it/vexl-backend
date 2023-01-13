package com.cleevio.vexl.common.service.query;

import javax.validation.constraints.NotBlank;

public record CheckSignatureValidityQuery(

        @NotBlank
        String publicKey,

        @NotBlank
        String hash,

        @NotBlank
        String signature

) {
}