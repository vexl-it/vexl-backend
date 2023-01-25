package com.cleevio.vexl.module.stats.event;

import javax.validation.constraints.NotBlank;

public record OffersDeletedEvent(
        @NotBlank
        int numberOfPublicParts,
        @NotBlank
        int numberOfPrivateParts) {
}
