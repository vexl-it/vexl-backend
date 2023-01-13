package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferPrivateCreate;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.entity.OfferPublicPart;
import com.cleevio.vexl.module.offer.exception.DuplicatedPublicKeyException;
import com.cleevio.vexl.module.offer.exception.IncorrectAdminIdFormatException;
import com.cleevio.vexl.module.offer.exception.MissingOwnerPrivatePartException;
import com.cleevio.vexl.module.offer.exception.OfferNotCreatedException;
import com.cleevio.vexl.module.offer.exception.OfferNotFoundException;
import com.cleevio.vexl.util.CreateOfferRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_1;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_2;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_UPDATE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

class OfferServiceTest {

    private static final String DUMMY_STRING_VALUE = "dummy_value";
    private static final String OFFER_ID = "dummy_offer_id";
    private static final String ADMIN_ID = "7452395b496020f8055d6137aaccc2072d19f473cbce51c64731235e6d87440b";
    private static final String INVALID_ADMIN_ID = "dummy_admin_id";
    private static final OfferPublicPart OFFER_PUBLIC_PART;
    private static final OfferPrivatePart OFFER_PRIVATE_PART;
    private static final Integer EXPIRATION_PERIOD = 14;

    private final OfferPublicRepository publicRepository = mock(OfferPublicRepository.class);
    private final OfferPrivateRepository privateRepository = mock(OfferPrivateRepository.class);
    private final MessageDigest messageDigest = mock(MessageDigest.class);
    private final AdvisoryLockService advisoryLockService = mock(AdvisoryLockService.class);
    private final OfferService offerService = new OfferService(
            publicRepository,
            privateRepository,
            messageDigest,
            advisoryLockService,
            EXPIRATION_PERIOD
    );

    static {
        OFFER_PUBLIC_PART = new OfferPublicPart();
        OFFER_PUBLIC_PART.setId(11L);

        OFFER_PRIVATE_PART = OfferPrivatePart.builder()
                .userPublicKey(USER_PUBLIC_KEY_1)
                .payloadPrivate(DUMMY_STRING_VALUE)
                .build();
    }

    @Test
    void createTest() throws OfferNotCreatedException {
        Mockito.when(publicRepository.save(any(OfferPublicPart.class))).thenReturn(OFFER_PUBLIC_PART);
        Mockito.when(privateRepository.save(any(OfferPrivatePart.class))).thenReturn(OFFER_PRIVATE_PART);
        Mockito.when(messageDigest.digest()).thenReturn(OFFER_ID.getBytes());
        Mockito.when(privateRepository.findByUserPublicKeyAndPublicPartId(any(), any())).thenReturn(Optional.of(OFFER_PRIVATE_PART));

        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestV2WithOnePrivatePart(), USER_PUBLIC_KEY_1);
        Mockito.verify(publicRepository).save(any());
        Mockito.verify(privateRepository).save(any());
    }

    @Test
    void updateTest() throws OfferNotFoundException {
        Mockito.when(publicRepository.findByAdminId(any(String.class))).thenReturn(Optional.of(OFFER_PUBLIC_PART));
        Mockito.when(privateRepository.save(any())).thenReturn(OFFER_PRIVATE_PART);
        Mockito.when(publicRepository.save(any())).thenReturn(OFFER_PUBLIC_PART);
        Mockito.when(privateRepository.findByUserPublicKeyAndPublicPartId(any(), any())).thenReturn(Optional.of(OFFER_PRIVATE_PART));

        offerService.updateOffer(CreateOfferRequestTestUtil.createUpdateOfferCommand(ADMIN_ID), USER_PUBLIC_KEY_UPDATE);
        Mockito.verify(publicRepository).save(any());
    }

    @Test
    void validatePrivateParts_shouldBeValid() {
        Mockito.when(publicRepository.save(any())).thenReturn(OFFER_PUBLIC_PART);
        Mockito.when(privateRepository.save(any())).thenReturn(OFFER_PRIVATE_PART);
        Mockito.when(messageDigest.digest()).thenReturn(OFFER_ID.getBytes());
        Mockito.when(privateRepository.findByUserPublicKeyAndPublicPartId(any(), any())).thenReturn(Optional.of(OFFER_PRIVATE_PART));
        final OfferPrivateCreate offerPrivateCreate = CreateOfferRequestTestUtil.createOfferPrivateCreate(USER_PUBLIC_KEY_1);
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestCustomPrivateParts(List.of(offerPrivateCreate)), USER_PUBLIC_KEY_1);
    }

    @Test
    void validatePrivateParts_shouldReturnMissingOwnerException() {
        final OfferPrivateCreate offerPrivateCreate = CreateOfferRequestTestUtil.createOfferPrivateCreate(USER_PUBLIC_KEY_1);

        assertThrows(
                MissingOwnerPrivatePartException.class,
                () -> offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestCustomPrivateParts(List.of(offerPrivateCreate)), USER_PUBLIC_KEY_2)
        );
    }

    @Test
    void validatePrivateParts_shouldReturnDuplicatedException() {
        final OfferPrivateCreate offerPrivateCreate = CreateOfferRequestTestUtil.createOfferPrivateCreate(USER_PUBLIC_KEY_1);
        final OfferPrivateCreate offerPrivateCreate2 = CreateOfferRequestTestUtil.createOfferPrivateCreate(USER_PUBLIC_KEY_1);

        assertThrows(
                DuplicatedPublicKeyException.class,
                () -> offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestCustomPrivateParts(List.of(offerPrivateCreate, offerPrivateCreate2)), USER_PUBLIC_KEY_1)
        );
    }

    @Test
    void refreshOffers_incorrectFormat_shouldReturnIncorrectAdminIdFormatException() {
        assertThrows(
                IncorrectAdminIdFormatException.class,
                () -> offerService.refreshOffers(CreateOfferRequestTestUtil.createOffersRefreshRequest(Set.of(INVALID_ADMIN_ID)), USER_PUBLIC_KEY_2)
        );
    }

    @Test
    void refreshOffers_correctFormat_shouldBeUpdated() {
        offerService.refreshOffers(CreateOfferRequestTestUtil.createOffersRefreshRequest(Set.of(ADMIN_ID)), USER_PUBLIC_KEY_2);

        Mockito.verify(publicRepository).refreshOffers(eq(List.of(ADMIN_ID)));
    }
}
