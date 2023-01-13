package com.cleevio.vexl.module.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record MemberRequest(

        @NotEmpty
        List<@NotNull GroupRequest> groups

) {

    public record GroupRequest(

            @NotBlank
            @Schema(required = true, description = "UUIDs hashes of the groups you are interested in.")
            String groupUuid,

            @NotNull
            @Schema(required = true, description = "Public keys of contacts you already have.")
            List<@NotBlank String> publicKeys


    ) {
    }

}
