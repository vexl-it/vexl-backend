package com.cleevio.vexl.util;

import com.cleevio.vexl.module.offer.constant.OfferType;
import com.cleevio.vexl.module.offer.dto.v1.request.DeletePrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.CreateOfferPrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferCreateRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferPrivateCreate;
import com.cleevio.vexl.module.offer.dto.v2.request.OffersRefreshRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.UpdateOfferRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOfferRequestTestUtil {

    public static final String USER_PUBLIC_KEY_1 = "dummy_user_public_key_1";
    public static final String USER_PUBLIC_KEY_2 = "dummy_user_public_key_2";
    public static final String USER_PUBLIC_KEY_3 = "dummy_user_public_key_3";
    public static final String USER_PUBLIC_KEY_UPDATE = "dummy_user_public_key_update";

    public static OfferCreateRequest createOfferCreateRequestV2WithOnePrivatePart() {
        return new OfferCreateRequest(
                OfferType.SELL.name(),
                "dummy_public_payload",
                List.of(createOfferPrivateCreate(USER_PUBLIC_KEY_1))
        );
    }

    public static OfferCreateRequest createOfferCreateRequestCustomPrivateParts(List<OfferPrivateCreate> privateParts) {
        return new OfferCreateRequest(
                OfferType.SELL.name(),
                "dummy_public_payload",
                privateParts
        );
    }

    public static OffersRefreshRequest createOffersRefreshRequest(Set<String> adminIds) {
        return new OffersRefreshRequest(
                adminIds
        );
    }

    public static OfferCreateRequest createOfferCreateRequest() {
        return new OfferCreateRequest(
                OfferType.SELL.name(),
                "dummy_public_payload",
                List.of(createOfferPrivateCreate(USER_PUBLIC_KEY_1), createOfferPrivateCreate2(USER_PUBLIC_KEY_2))
        );
    }

    public static CreateOfferPrivatePartRequest createCreateOfferPrivatePartRequest(String adminId, String userPublicKey) {
        return new CreateOfferPrivatePartRequest(
                adminId,
                List.of(createOfferPrivateCreate(userPublicKey))
        );
    }

    public static DeletePrivatePartRequest createDeletePrivatePartRequest(List<String> adminIds, List<String> publicKeys) {
        return new DeletePrivatePartRequest(
                adminIds,
                publicKeys
        );
    }

    public static UpdateOfferRequest createUpdateOfferCommand(String adminId) {
        return new UpdateOfferRequest(
                adminId,
                "dummy_payload_public",
                List.of(updateOfferPrivateCreate())
        );
    }

    public static OfferPrivateCreate createOfferPrivateCreate(String publicKey) {
        return new OfferPrivateCreate(
                publicKey,
                "dummy_payload_private"
        );
    }

    public static OfferPrivateCreate createOfferPrivateCreate2(String publicKey) {
        return new OfferPrivateCreate(
                publicKey,
                "dummy_payload_private_2"
        );
    }

    private static OfferPrivateCreate updateOfferPrivateCreate() {
        return new OfferPrivateCreate(
                USER_PUBLIC_KEY_UPDATE,
                "dummy_payload_private"
        );
    }

    public static OfferCreateRequest createOfferCreateRequestWithPublicKeyAndOfferType(OfferType offerType, String publicKey) {
        return new OfferCreateRequest(
                offerType.name(),
                "dummy_public_payload",
                List.of(createOfferPrivateCreate(publicKey), createOfferPrivateCreate2(USER_PUBLIC_KEY_2))
        );
    }

}
