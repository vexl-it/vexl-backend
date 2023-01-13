package com.cleevio.vexl.module.group.event;

import javax.validation.constraints.NotBlank;

public record GroupLeftEvent(

        @NotBlank
        String hash,

        @NotBlank
        String groupUuidHash

) {
}
