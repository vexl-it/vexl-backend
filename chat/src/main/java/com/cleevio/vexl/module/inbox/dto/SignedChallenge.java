package com.cleevio.vexl.module.inbox.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record SignedChallenge(

        @NotBlank
        @Schema(required = true, description = "Signed challenge.")
        String challenge,

        @NotBlank
        @Schema(required = true, description = """
                To verify that the client owns the private key to the public key that he claims is his.\040
                First you need to retrieve challenge for verification in challenge API. Then sign it with private key and the signature send here.
                """)
        String signature

) {
}
