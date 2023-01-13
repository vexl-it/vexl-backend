package com.cleevio.vexl.module.offer.dto.v1.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.util.List;

public record DeletePrivatePartRequest(

        @Schema(required = true, description = "Offer IDs where you want to delete private part.")
        List<@NotBlank String> adminIds,

        @Schema(required = true, description = "Public key is ID of an private part.")
        List<@NotBlank String> publicKeys

) {
}
