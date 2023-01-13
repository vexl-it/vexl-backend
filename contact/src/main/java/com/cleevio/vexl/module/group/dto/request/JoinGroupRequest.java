package com.cleevio.vexl.module.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record JoinGroupRequest (

    @Schema(required = true, description = "QR code of group to join to.")
    int code

) {
}
