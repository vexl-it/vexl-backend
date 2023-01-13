package com.cleevio.vexl.module.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.util.List;

public record ExpiredGroupsRequest(

    @Schema(required = true, description = "Hashed UUID.")
    List<@NotBlank String> uuids

) {
}
