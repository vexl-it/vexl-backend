package com.cleevio.vexl.module.offer.dto.v2.response;

import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.serializer.DateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OfferUnifiedResponse {

    @Schema(description = "ID of the offer. It should be used for ordering.")
    private final Long id;

    @Schema(description = "265-bit Offer ID")
    private final String offerId;

    @Schema(description = "Encrypted public payload. It should be encrypted by client with symmetric encryption.")
    private final String publicPayload;

    @Schema(description = "Encrypted private payload. It should be encrypted by client with asymmetric encryption.")
    private final String privatePayload;

    @Schema(description = "Expiration is managed via refreshed_at field. Expiration is not used anymore and is in the model only for backwards compatibility.")
    private final Long expiration = 96339579318L;

    @JsonSerialize(using = DateTimeSerializer.class)
    private final LocalDate createdAt;

    @JsonSerialize(using = DateTimeSerializer.class)
    private final LocalDate modifiedAt;

    public OfferUnifiedResponse(OfferPrivatePart offerPrivatePart) {
        this.id = offerPrivatePart.getOfferPublicPart().getId();
        this.offerId = offerPrivatePart.getOfferPublicPart().getOfferId();
        this.publicPayload = offerPrivatePart.getOfferPublicPart().getPayloadPublic();
        this.privatePayload = offerPrivatePart.getPayloadPrivate();
        this.createdAt = offerPrivatePart.getOfferPublicPart().getCreatedAt();
        this.modifiedAt = offerPrivatePart.getOfferPublicPart().getModifiedAt();
    }
}
