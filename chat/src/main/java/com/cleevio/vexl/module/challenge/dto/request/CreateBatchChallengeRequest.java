package com.cleevio.vexl.module.challenge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateBatchChallengeRequest(

        @NotEmpty
        @Schema(required = true, description = "Public keys for which I want to create challenges.")
        Set<@NotBlank String> publicKeys

) {
}
