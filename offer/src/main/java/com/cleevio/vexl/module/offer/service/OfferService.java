package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.offer.constant.OfferAdvisoryLock;
import com.cleevio.vexl.module.offer.constant.OfferType;
import com.cleevio.vexl.module.offer.dto.v1.request.DeletePrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v1.request.NotExistingOffersRequest;
import com.cleevio.vexl.module.offer.dto.v1.request.ReportOfferRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.CreateOfferPrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferCreateRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferPrivateCreate;
import com.cleevio.vexl.module.offer.dto.v2.request.OffersRefreshRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.UpdateOfferRequest;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.entity.OfferPublicPart;
import com.cleevio.vexl.module.offer.exception.DuplicatedPublicKeyException;
import com.cleevio.vexl.module.offer.exception.IncorrectAdminIdFormatException;
import com.cleevio.vexl.module.offer.exception.MissingOwnerPrivatePartException;
import com.cleevio.vexl.module.offer.exception.OfferNotFoundException;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Offer service implements importing, filtering and deleting of an offer, which is divided to public part and private part.
 * <p>
 * Public part is whole unencrypted.
 * Private part is whole encrypted except for the userPublicKey, which is an identifier that allows us to find which user can decrypt
 * this encrypted part with his/her private key.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class OfferService {

    private final OfferPublicRepository offerPublicRepository;
    private final OfferPrivateRepository offerPrivateRepository;
    private final MessageDigest messageDigest;
    private final AdvisoryLockService advisoryLockService;
    @Value("${offer.expiration}")
    private final Integer expirationPeriod;
    private static final long ONE = 1;
    private static final int SIXTY_FOUR = 64;

    /**
     * Creating private and public part of an offer from request.
     */
    @Transactional
    public OfferPrivatePart createOffer(@Valid final OfferCreateRequest request, final String publicKey) {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.CREATE.name(),
                publicKey
        );

        validatePrivateParts(
                request.offerPrivateList().stream()
                        .map(OfferPrivateCreate::userPublicKey)
                        .toList(),
                publicKey
        );

        final OfferPublicPart offerPublicPart = OfferPublicPart.builder()
                .adminId(generateKeyValue())
                .offerId(generateKeyValue())
                .offerType(request.offerType() == null ? null : OfferType.valueOf(request.offerType().toUpperCase()))
                .refreshedAt(LocalDate.now())
                .modifiedAt(LocalDate.now())
                .payloadPublic(request.payloadPublic())
                .build();

        final OfferPublicPart savedPublicPart = this.offerPublicRepository.save(offerPublicPart);

        log.info("Saved public part of offer with id: {}",
                savedPublicPart.getId());

        createOfferPrivateParts(request.offerPrivateList(), savedPublicPart);

        return findOfferByPublicKeyAndPublicPartId(publicKey, savedPublicPart.getOfferId());
    }

    /**
     * Find offers by public key. User knows his private nad public key. He will find all offers for him by his public key.
     * Then he will be able to decrypt them with his private key, which he has in the device.
     */
    @Transactional(readOnly = true)
    public List<OfferPrivatePart> findOffersByPublicKey(final String publicKey) {
        return this.offerPrivateRepository.findAllByUserPublicKey(publicKey);
    }

    /**
     * Since the server cannot filter in the encrypted data, the FE must maintain all Offers locally in order to filter in them.
     * This method is used to retrieve Offers that the FE does not already have stored locally.
     */
    @Transactional(readOnly = true)
    public List<OfferPrivatePart> getNewOrModifiedOffers(LocalDate modifiedAt, String publicKey) {
        return this.offerPrivateRepository.findAllByUserPublicKeyAndModifiedAt(
                publicKey, modifiedAt
        );
    }

    @Transactional(readOnly = true)
    public OfferPrivatePart findOfferByPublicKeyAndPublicPartId(String publicKey, String offerId) {
        return offerPrivateRepository.findByUserPublicKeyAndPublicPartId(
                publicKey,
                offerId
        ).orElseThrow(OfferNotFoundException::new);
    }

    /**
     * Will delete all private parts of an offer and public part as well.
     */
    @Transactional
    public void deleteOffers(List<String> adminIds) {
        if (adminIds.isEmpty()) return;

        this.offerPrivateRepository.deleteAllPrivatePartsByAdminIds(adminIds);
        log.info("Deleted all private parts of an offer.");

        this.offerPublicRepository.deleteByAdminIds(adminIds);
        log.info("Deleted public part of an offer.");
    }

    @Transactional
    public OfferPrivatePart updateOffer(@Valid UpdateOfferRequest request, String publicKey) {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.MODIFY.name(),
                request.adminId()
        );

        OfferPublicPart publicPart = this.offerPublicRepository.findByAdminId(request.adminId())
                .orElseThrow(OfferNotFoundException::new);

        if (!request.offerPrivateList().isEmpty()) {
            validatePrivateParts(
                    request.offerPrivateList().stream()
                            .map(OfferPrivateCreate::userPublicKey)
                            .toList(),
                    publicKey
            );

            this.offerPrivateRepository.deleteAllPrivatePartsByAdminIds(List.of(request.adminId()));
            createOfferPrivateParts(request.offerPrivateList(), publicPart);
        }

        if (request.payloadPublic() != null) {
            publicPart.setPayloadPublic(request.payloadPublic());
        }

        publicPart.setModifiedAt(LocalDate.now());

        final OfferPublicPart updatedPublicPart = this.offerPublicRepository.save(publicPart);
        log.info("Offer [{}] has been successfully updated.", updatedPublicPart.getOfferId());

        return findOfferByPublicKeyAndPublicPartId(publicKey, updatedPublicPart.getOfferId());
    }

    @Transactional(readOnly = true)
    public List<OfferPrivatePart> findOffersByIdsAndPublicKey(List<String> offerIds, String publicKey) {
        return this.offerPrivateRepository.findOfferByPublicKeyAndPublicPartIds(publicKey, offerIds);
    }

    @Transactional
    public void removeExpiredOffers() {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.REMOVING_TASK.name()
        );

        try {
            final LocalDate expiration = LocalDate.now().minusDays(expirationPeriod);

            log.info("Deleting all offers older then [{}].", expiration);

            this.offerPrivateRepository.deleteAllExpiredPrivateParts(expiration);
            this.offerPublicRepository.deleteAllExpiredPublicParts(expiration);
        } catch (Exception e) {
            log.error("Error while removing expired offers: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteOfferByOfferIdAndPublicKey(@Valid DeletePrivatePartRequest request) {
        if (request.publicKeys().isEmpty() || request.adminIds().isEmpty()) return;
        this.offerPrivateRepository.deletePrivatePartOfferByAdminIdsAndPublicKeys(request.adminIds(), request.publicKeys());
    }

    @Transactional
    public void addPrivatePartsOffer(@Valid final CreateOfferPrivatePartRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.MODIFY.name(),
                request.adminId()
        );

        final List<OfferPrivateCreate> privatePartsToCreate = new ArrayList<>();
        final Set<String> publicKeys = new HashSet<>();
        request.offerPrivateList()
                .forEach(pp -> {
                    if (publicKeys.add(pp.userPublicKey())) {
                        privatePartsToCreate.add(pp);
                    }
                });

        if (privatePartsToCreate.isEmpty()) {
            return;
        }

        final OfferPublicPart offerPublicPart = this.offerPublicRepository.findByAdminId(request.adminId())
                .orElseThrow(OfferNotFoundException::new);

        removePrivatePartIfAlreadyExists(publicKeys, request.adminId());
        createOfferPrivateParts(privatePartsToCreate, offerPublicPart);
        offerPublicPart.setModifiedAt(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<String> retrieveNotExistingOfferIds(@Valid final NotExistingOffersRequest request, final String publicKey) {
        List<String> offerIds = request.offerIds();

        final List<String> existingOfferIds = this.offerPrivateRepository.findExistingOfferIds(offerIds, publicKey);

        offerIds.removeAll(existingOfferIds);
        return offerIds;
    }

    @Transactional
    public void reportOffer(@Valid final ReportOfferRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.REPORT.name(),
                request.offerId()
        );

        this.offerPublicRepository.findByOfferId(request.offerId())
                .ifPresentOrElse(
                        this::increaseReportAndSave,
                        () -> log.warn("Offer [{}] does not exist", request.offerId())
                );
    }

    @Transactional
    public void refreshOffers(@Valid final OffersRefreshRequest request, final String publicKey) {
        advisoryLockService.lock(
                ModuleLockNamespace.OFFER,
                OfferAdvisoryLock.REFRESH.name(),
                publicKey
        );

        final List<String> adminIds = request.adminIds()
                .stream()
                .map(String::trim)
                .toList();

        if (!areAdminIdsInCorrectFormat(adminIds)) {
            throw new IncorrectAdminIdFormatException();
        }

        this.offerPublicRepository.refreshOffers(adminIds);
    }

    @Transactional(readOnly = true)
    public List<StatsDto> retrieveStats(final StatsKey... statsKeys) {
        final var minusOneDay = LocalDate.now().minusDays(ONE);
        final var medianWithPercentageCountBuy = this.offerPublicRepository.getMedianWithPercentageCount(OfferType.BUY.name());
        final var medianWithPercentageCountSell = this.offerPublicRepository.getMedianWithPercentageCount(OfferType.SELL.name());
        final List<StatsDto> statsDtos = new ArrayList<>();
        Arrays.stream(statsKeys).forEach(statKey -> {
            switch (statKey) {
                case MODIFIED_COUNT_BUY -> statsDtos.add(new StatsDto(
                        StatsKey.MODIFIED_COUNT_BUY,
                        this.offerPublicRepository.getModifiedOffersCount(minusOneDay, OfferType.BUY)
                ));
                case MODIFIED_COUNT_SELL -> statsDtos.add(new StatsDto(
                        StatsKey.MODIFIED_COUNT_SELL,
                        this.offerPublicRepository.getModifiedOffersCount(minusOneDay, OfferType.SELL)
                ));
                case ACTIVE_COUNT_BUY -> statsDtos.add(new StatsDto(
                        StatsKey.ACTIVE_COUNT_BUY,
                        this.offerPublicRepository.getActiveOffersCount(OfferType.BUY)
                ));
                case ACTIVE_COUNT_SELL -> statsDtos.add(new StatsDto(
                        StatsKey.ACTIVE_COUNT_SELL,
                        this.offerPublicRepository.getActiveOffersCount(OfferType.SELL)
                ));
                case ALL_TIME_COUNT -> statsDtos.add(new StatsDto(
                        StatsKey.ALL_TIME_COUNT,
                        this.offerPublicRepository.getAllTimeCount()
                ));
                case OFFER_PERC_5_BUY -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_5_BUY,
                        ifNullReturnZero(medianWithPercentageCountBuy.getPercentage5())
                ));
                case OFFER_PERC_50_BUY -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_50_BUY,
                        ifNullReturnZero(medianWithPercentageCountBuy.getPercentage50())
                ));
                case OFFER_PERC_95_BUY -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_95_BUY,
                        ifNullReturnZero(medianWithPercentageCountBuy.getPercentage95())
                ));
                case OFFER_PERC_5_SELL -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_5_SELL,
                        ifNullReturnZero(medianWithPercentageCountSell.getPercentage5())
                ));
                case OFFER_PERC_50_SELL -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_50_SELL,
                        ifNullReturnZero(medianWithPercentageCountSell.getPercentage50())
                ));
                case OFFER_PERC_95_SELL -> statsDtos.add(new StatsDto(
                        StatsKey.OFFER_PERC_95_SELL,
                        ifNullReturnZero(medianWithPercentageCountSell.getPercentage95())
                ));
            }
        });
        return statsDtos;
    }

    private void removePrivatePartIfAlreadyExists(Set<String> publicKeys, String adminId) {
        if (this.offerPrivateRepository.existsByUserPublicKeysAndAdminId(publicKeys, adminId)) {
            log.warn("""
                    There is(are) already created private part(s) with the public key.
                    The private part(s) will be deleted and new will be created.
                    """);
            this.offerPrivateRepository.deletePrivatePartOfferByAdminIdAndPublicKeys(adminId, publicKeys);
        }
    }

    private int ifNullReturnZero(@Nullable final Integer value) {
        return value == null ? 0 : value;
    }

    private void increaseReportAndSave(final OfferPublicPart pp) {
        pp.setReport(pp.getReport() + 1);
        this.offerPublicRepository.save(pp);
    }

    /**
     * Generating 256-bit key-value
     */
    private String generateKeyValue() {
        messageDigest.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(messageDigest.digest());
    }

    private void validatePrivateParts(List<String> userPublicKey, String publicKey) {
        userPublicKey
                .stream()
                .filter(it -> it.equals(publicKey))
                .findFirst()
                .orElseThrow(MissingOwnerPrivatePartException::new);

        Set<String> items = new HashSet<>();
        final boolean duplicate = userPublicKey
                .stream()
                .anyMatch(it -> !items.add(it));

        if (duplicate) {
            throw new DuplicatedPublicKeyException();
        }
    }

    private void createOfferPrivateParts(List<OfferPrivateCreate> offerPrivateCreateList, OfferPublicPart savedPublicPart) {
        offerPrivateCreateList.forEach(o -> creatSingleOfferPrivatePart(o, savedPublicPart));
    }

    private void creatSingleOfferPrivatePart(OfferPrivateCreate privateCreate, OfferPublicPart offerPublicPart) {
        final OfferPrivatePart offerPrivatePart = OfferPrivatePart.builder()
                .userPublicKey(privateCreate.userPublicKey())
                .payloadPrivate(privateCreate.payloadPrivate())
                .offerPublicPart(offerPublicPart)
                .build();

        this.offerPrivateRepository.save(offerPrivatePart);
    }

    private boolean areAdminIdsInCorrectFormat(List<String> adminIds) {
        return adminIds
                .stream()
                .noneMatch(it -> it.length() != SIXTY_FOUR);
    }
}