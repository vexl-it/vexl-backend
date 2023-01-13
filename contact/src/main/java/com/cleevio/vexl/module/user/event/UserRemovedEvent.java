package com.cleevio.vexl.module.user.event;

import javax.validation.constraints.NotBlank;

public record UserRemovedEvent(

        @NotBlank
        String publicKey

) {
}
