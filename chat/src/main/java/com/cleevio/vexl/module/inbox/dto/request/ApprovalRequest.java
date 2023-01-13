package com.cleevio.vexl.module.inbox.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record ApprovalRequest(

        @NotBlank
        @Schema(required = true, description = "Public key of inbox you want to approval from.")
        String publicKey,

        @NotBlank
        @Schema(required = true, description = "Approval message for an user.")
        String message

) {
}
