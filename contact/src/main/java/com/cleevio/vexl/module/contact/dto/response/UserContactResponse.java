package com.cleevio.vexl.module.contact.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserContactResponse(

        @Schema(description = "PublicKey in Base64.")
        String publicKey

) {
}
