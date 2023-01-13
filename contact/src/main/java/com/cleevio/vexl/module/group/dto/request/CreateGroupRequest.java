package com.cleevio.vexl.module.group.dto.request;

import com.cleevio.vexl.module.file.dto.request.ImageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

public record CreateGroupRequest(

        @NotBlank
        @Schema(required = true)
        String name,

        @Nullable
        @Schema(description = "Base64 encoded file data including header. i.e.: data:image/png;base64,iVBORw0KGgo")
        ImageRequest logo,

        @Positive
        @Schema(required = true, description = "When the group will be deleted. Unix timestamp seconds format.")
        long expirationAt,

        @Positive
        @Schema(required = true, description = "Since no-one will be able to join the group. Unix timestamp seconds format.")
        long closureAt

) {
}
