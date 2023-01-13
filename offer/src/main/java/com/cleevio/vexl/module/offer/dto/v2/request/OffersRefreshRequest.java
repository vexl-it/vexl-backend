package com.cleevio.vexl.module.offer.dto.v2.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record OffersRefreshRequest(

        @NotEmpty
        @Schema(required = true, description = "Admin ids of the offers to refresh.")
        Set<@NotBlank String> adminIds

) {
}
