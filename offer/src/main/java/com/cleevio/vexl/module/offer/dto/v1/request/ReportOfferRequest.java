package com.cleevio.vexl.module.offer.dto.v1.request;

import javax.validation.constraints.NotBlank;

public record ReportOfferRequest(

        @NotBlank
        String offerId

) {
}
