package com.cleevio.vexl.module.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public record LeaveGroupRequest (

    @NotBlank
    @Schema(required = true, description = "SHA-256 Group UUID")
    String groupUuid

) {
}
