package com.cleevio.vexl.module.offer.dto.v2.request;

import javax.validation.constraints.NotBlank;

public record OfferPrivateCreate(

        @NotBlank
        String userPublicKey,

        @NotBlank
        String payloadPrivate

) {
}
