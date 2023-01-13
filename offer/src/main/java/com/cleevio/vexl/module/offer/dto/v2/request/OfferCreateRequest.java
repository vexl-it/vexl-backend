package com.cleevio.vexl.module.offer.dto.v2.request;

import com.cleevio.vexl.common.annotation.NullOrNotBlank;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record OfferCreateRequest(

        @Nullable
        @NullOrNotBlank
        String offerType,

        @NotBlank
        String payloadPublic,

        @Valid
        @NotEmpty
        List<@NotNull OfferPrivateCreate> offerPrivateList

) {
}
