package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.offer.dto.v2.request.CreateOfferPrivatePartRequest;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.exception.OfferNotFoundException;
import com.cleevio.vexl.util.CreateOfferRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_1;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_2;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_3;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_UPDATE;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.createOfferPrivateCreate2;
import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.createOfferPrivateCreate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfferServiceIT {

    private final OfferService offerService;
    private final OfferPrivateRepository privateRepository;
    private final OfferPublicRepository publicRepository;

    @Autowired
    public OfferServiceIT(OfferService offerService, OfferPrivateRepository privateRepository, OfferPublicRepository publicRepository) {
        this.offerService = offerService;
        this.privateRepository = privateRepository;
        this.publicRepository = publicRepository;
    }

    @Test
    void testCreate_shouldCreateOffer() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final var privateCreateRequest = request.offerPrivateList().get(0);

        final var createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);
        final List<OfferPrivatePart> offer = offerService.findOffersByIdsAndPublicKey(List.of(createdOffer.getOfferPublicPart().getOfferId()), USER_PUBLIC_KEY_1);

        assertThat(offer).hasSize(1);

        final OfferPrivatePart unifiedResponse = offer.get(0);
        assertThat(unifiedResponse.getPayloadPrivate()).isEqualTo(privateCreateRequest.payloadPrivate());
        assertThat(unifiedResponse.getUserPublicKey()).isEqualTo(privateCreateRequest.userPublicKey());
        assertThat(unifiedResponse.getOfferPublicPart().getPayloadPublic()).isEqualTo(request.payloadPublic());
    }

    @Test
    void testUpdate_shouldUpdate() {
        final var createOffer = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final var createdOffer = offerService.createOffer(createOffer, USER_PUBLIC_KEY_1);
        final var adminId = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, createdOffer.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        final var updateRequest = CreateOfferRequestTestUtil.createUpdateOfferCommand(adminId);
        final var privateUpdateRequest = updateRequest.offerPrivateList().get(0);
        final var updatedOffer = offerService.updateOffer(updateRequest, USER_PUBLIC_KEY_UPDATE);

        final List<OfferPrivatePart> offer = offerService.findOffersByIdsAndPublicKey(List.of(updatedOffer.getOfferPublicPart().getOfferId()), privateUpdateRequest.userPublicKey());

        assertThat(offer).hasSize(1);

        final OfferPrivatePart unifiedResponse = offer.get(0);
        assertThat(unifiedResponse.getPayloadPrivate()).isEqualTo(privateUpdateRequest.payloadPrivate());
        assertThat(unifiedResponse.getUserPublicKey()).isEqualTo(privateUpdateRequest.userPublicKey());
        assertThat(unifiedResponse.getOfferPublicPart().getPayloadPublic()).isEqualTo(updateRequest.payloadPublic());
    }

    @Test
    void testUpdateWithOfferId_invalidInput_shouldThrowOfferNotFoundException() {
        final var createOfferRequest = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final var createdOffer = offerService.createOffer(createOfferRequest, USER_PUBLIC_KEY_1);

        final var updateRequest = CreateOfferRequestTestUtil.createUpdateOfferCommand(createdOffer.getOfferPublicPart().getOfferId());
        assertThrows(
                OfferNotFoundException.class,
                () -> offerService.updateOffer(updateRequest, USER_PUBLIC_KEY_UPDATE)
        );
    }

    @Test
    void testNewOrModifiedOffers_shouldFind() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final var privateCreateRequest = request.offerPrivateList().get(0);

        offerService.createOffer(request, USER_PUBLIC_KEY_1);
        final List<OfferPrivatePart> offer = offerService.getNewOrModifiedOffers(LocalDate.now().minus(Period.ofDays(1)), USER_PUBLIC_KEY_1);

        assertThat(offer).hasSize(1);

        final OfferPrivatePart unifiedResponse = offer.get(0);
        assertThat(unifiedResponse.getPayloadPrivate()).isEqualTo(privateCreateRequest.payloadPrivate());
        assertThat(unifiedResponse.getUserPublicKey()).isEqualTo(privateCreateRequest.userPublicKey());
        assertThat(unifiedResponse.getOfferPublicPart().getPayloadPublic()).isEqualTo(request.payloadPublic());
    }

    @Test
    void testNewOrModifiedOffers_shouldNotFindAny() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();

        offerService.createOffer(request, USER_PUBLIC_KEY_1);
        final List<OfferPrivatePart> offer = offerService.getNewOrModifiedOffers(LocalDate.now().plus(Period.ofDays(1)), USER_PUBLIC_KEY_1);

        assertThat(offer).hasSize(0);
    }

    @Test
    void testDeleteOffers_shouldBeDeleted() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final var request2 = CreateOfferRequestTestUtil.createOfferCreateRequest();

        final OfferPrivatePart offer1 = offerService.createOffer(request, USER_PUBLIC_KEY_1);
        final OfferPrivatePart offer2 = offerService.createOffer(request2, USER_PUBLIC_KEY_2);

        final var adminId1 = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, offer1.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();
        final var adminId2 = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_2, offer2.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        assertThat(privateRepository.findAll()).hasSize(4);
        assertThat(publicRepository.findAll()).hasSize(2);

        offerService.deleteOffers(List.of(adminId1, adminId2));

        assertThat(privateRepository.findAll()).hasSize(0);
        assertThat(publicRepository.findAll()).hasSize(0);
    }

    @Test
    void removeOnePrivatePart_shouldBeRemoved() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();

        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        final var adminId = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, createdOffer.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        offerService.deleteOfferByOfferIdAndPublicKey(CreateOfferRequestTestUtil.createDeletePrivatePartRequest(List.of(adminId), List.of(USER_PUBLIC_KEY_2)));

        List<OfferPrivatePart> privateParts = privateRepository.findAll();

        assertThat(privateParts).hasSize(1);
        assertThat(publicRepository.findAll()).hasSize(1);
        assertThat(privateParts.get(0).getUserPublicKey()).isEqualTo(USER_PUBLIC_KEY_1);
    }

    @Test
    void removeOnePrivatePartWithOfferId_invalidInput_shouldDeleteNothing() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();

        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        offerService.deleteOfferByOfferIdAndPublicKey(CreateOfferRequestTestUtil.createDeletePrivatePartRequest(List.of(createdOffer.getOfferPublicPart().getOfferId()), List.of(USER_PUBLIC_KEY_2)));

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);
    }

    @Test
    void addPrivatePartOffer_shouldBeAdded() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        final var adminId = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, createdOffer.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        offerService.addPrivatePartsOffer(CreateOfferRequestTestUtil.createCreateOfferPrivatePartRequest(adminId, USER_PUBLIC_KEY_3));

        final var privateParts = privateRepository.findAll();
        assertThat(privateParts).hasSize(3);
        assertThat(publicRepository.findAll()).hasSize(1);
        assertThat(privateParts.get(2).getUserPublicKey()).isEqualTo(USER_PUBLIC_KEY_3);
    }

    @Test
    void addPrivatePartOfferWithOfferId_invalidInput_shouldThrowOfferNotFoundException() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        assertThrows(
                OfferNotFoundException.class,
                () -> offerService.addPrivatePartsOffer(CreateOfferRequestTestUtil.createCreateOfferPrivatePartRequest(createdOffer.getOfferPublicPart().getOfferId(), USER_PUBLIC_KEY_3))
        );

    }

    @Test
    void addPrivatePartOffer_duplicatedPublicKey_shouldRemoveAndRecreatePrivatePart() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        final var adminId = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, createdOffer.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        offerService.addPrivatePartsOffer(CreateOfferRequestTestUtil.createCreateOfferPrivatePartRequest(adminId, USER_PUBLIC_KEY_1));

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);
    }

    @Test
    void addPrivatePartOffer_duplicatedPublicKeyInRequest_shouldCreateOnlyOnce() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();

        final OfferPrivatePart createdOffer = offerService.createOffer(request, USER_PUBLIC_KEY_1);

        final var adminId = offerService.findOfferByPublicKeyAndPublicPartId(USER_PUBLIC_KEY_1, createdOffer.getOfferPublicPart().getOfferId())
                .getOfferPublicPart().getAdminId();

        final var offerPublicPartsBeforeAddedNewPrivateParts = privateRepository.findAll().stream().map(OfferPrivatePart::getUserPublicKey).toList();
        assertThat(offerPublicPartsBeforeAddedNewPrivateParts).containsOnly(USER_PUBLIC_KEY_1, USER_PUBLIC_KEY_2);
        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        final var requestWithDuplicatePublicKey = new CreateOfferPrivatePartRequest(
                adminId,
                List.of(createOfferPrivateCreate(USER_PUBLIC_KEY_3), createOfferPrivateCreate2(USER_PUBLIC_KEY_3))
        );

        offerService.addPrivatePartsOffer(requestWithDuplicatePublicKey);

        assertThat(privateRepository.findAll()).hasSize(3);
        assertThat(publicRepository.findAll()).hasSize(1);
        final var offerPublicPartsAfterAddedNewPrivateParts = privateRepository.findAll().stream().map(OfferPrivatePart::getUserPublicKey).toList();
        assertThat(offerPublicPartsAfterAddedNewPrivateParts).containsOnly(USER_PUBLIC_KEY_1, USER_PUBLIC_KEY_2, USER_PUBLIC_KEY_3);
    }

    @Test
    void addPrivatePartOffer_noExistingOfferId_shouldReturnOfferNotFoundException() {
        final var request = CreateOfferRequestTestUtil.createOfferCreateRequest();
        offerService.createOffer(request, USER_PUBLIC_KEY_1);

        assertThat(privateRepository.findAll()).hasSize(2);
        assertThat(publicRepository.findAll()).hasSize(1);

        assertThrows(
                OfferNotFoundException.class,
                () -> offerService.addPrivatePartsOffer(CreateOfferRequestTestUtil.createCreateOfferPrivatePartRequest("random_admin_id", USER_PUBLIC_KEY_1))
        );
    }
}
