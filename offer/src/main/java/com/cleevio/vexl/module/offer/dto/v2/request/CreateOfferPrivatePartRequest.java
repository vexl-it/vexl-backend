package com.cleevio.vexl.module.offer.dto.v2.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record CreateOfferPrivatePartRequest(

        @NotBlank
        String adminId,

        @Valid
        @NotEmpty
        List<@NotNull OfferPrivateCreate> offerPrivateList

) {
}
