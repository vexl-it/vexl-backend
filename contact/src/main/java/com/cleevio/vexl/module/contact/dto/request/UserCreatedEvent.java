package com.cleevio.vexl.module.contact.dto.request;

import javax.validation.constraints.NotNull;

public record UserCreatedEvent(
        @NotNull
        String numberHash,

        @NotNull
        String publicKey
){}
