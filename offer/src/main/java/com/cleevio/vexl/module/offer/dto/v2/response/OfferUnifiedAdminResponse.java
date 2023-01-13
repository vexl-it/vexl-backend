package com.cleevio.vexl.module.offer.dto.v2.response;

import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public final class OfferUnifiedAdminResponse extends OfferUnifiedResponse {

    @Schema(description = "265-bit Admin ID")
    private final String adminId;

    public OfferUnifiedAdminResponse(OfferPrivatePart offerPrivatePart) {
        super(offerPrivatePart);
        this.adminId = offerPrivatePart.getOfferPublicPart().getAdminId();
    }
}
