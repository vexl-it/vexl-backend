package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.offer.constant.OfferType;
import com.cleevio.vexl.module.offer.service.OfferService;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.util.CreateOfferRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsServiceIT {

    private final StatsService statsService;
    private final StatsRepository repository;
    private final OfferService offerService;

    private static final String PUBLIC_KEY_1 = "public_key_1";
    private static final String PUBLIC_KEY_2 = "public_key_2";
    private static final String PUBLIC_KEY_3 = "public_key_3";
    private static final String PUBLIC_KEY_4 = "public_key_4";
    private static final String PUBLIC_KEY_5 = "public_key_5";

    @Autowired
    public StatsServiceIT(StatsService statsService, StatsRepository repository,
                          OfferService offerService) {
        this.statsService = statsService;
        this.repository = repository;
        this.offerService = offerService;
    }

    @Test
    void testProcessStats_shouldBeProcessed() {
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestWithPublicKeyAndOfferType(OfferType.BUY, PUBLIC_KEY_1), PUBLIC_KEY_1);
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestWithPublicKeyAndOfferType(OfferType.SELL, PUBLIC_KEY_2), PUBLIC_KEY_2);
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestWithPublicKeyAndOfferType(OfferType.BUY, PUBLIC_KEY_3), PUBLIC_KEY_3);
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestWithPublicKeyAndOfferType(OfferType.SELL, PUBLIC_KEY_4), PUBLIC_KEY_4);
        offerService.createOffer(CreateOfferRequestTestUtil.createOfferCreateRequestWithPublicKeyAndOfferType(OfferType.BUY, PUBLIC_KEY_5), PUBLIC_KEY_5);

        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(11);

        final var activeCountBuy = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_COUNT_BUY)).findFirst().get();
        final var activeCountSell = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_COUNT_SELL)).findFirst().get();
        final var allTimeCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ALL_TIME_COUNT)).findFirst().get();
        final var modifiedCountBuy = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MODIFIED_COUNT_BUY)).findFirst().get();
        final var modifiedCountSell = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MODIFIED_COUNT_SELL)).findFirst().get();
        assertThat(activeCountBuy.getValue()).isEqualTo(3);
        assertThat(activeCountSell.getValue()).isEqualTo(2);
        assertThat(allTimeCount.getValue()).isEqualTo(5);
        assertThat(modifiedCountBuy.getValue()).isEqualTo(3);
        assertThat(modifiedCountSell.getValue()).isEqualTo(2);
    }

    @Test
    void testProcessStats__emptyDatabase_shouldBeProcessed() {
        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(11);

        final var activeCountBuy = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_COUNT_BUY)).findFirst().get();
        final var activeCountSell = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_COUNT_SELL)).findFirst().get();
        final var modifiedCountBuy = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MODIFIED_COUNT_BUY)).findFirst().get();
        final var modifiedCountSell = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MODIFIED_COUNT_SELL)).findFirst().get();
        assertThat(activeCountBuy.getValue()).isEqualTo(0);
        assertThat(activeCountSell.getValue()).isEqualTo(0);
        assertThat(modifiedCountBuy.getValue()).isEqualTo(0);
        assertThat(modifiedCountSell.getValue()).isEqualTo(0);
    }
}
