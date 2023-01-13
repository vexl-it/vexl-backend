package com.cleevio.vexl.module.offer.dto.v1.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public record NotExistingOffersRequest(

        @NotEmpty
        List<@NotBlank String> offerIds

) {
}
