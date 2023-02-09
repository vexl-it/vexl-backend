package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.offer.service.OfferService;
import com.cleevio.vexl.module.stats.constant.StatsAdvisoryLock;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.stats.entity.Stats;
import com.cleevio.vexl.module.stats.event.OffersDeletedEvent;
import com.cleevio.vexl.module.stats.event.OffersExpiredEvent;
import com.cleevio.vexl.module.stats.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AdvisoryLockService advisoryLockService;
    private final OfferService offerService;
    private final StatsRepository repository;
    private final StatsMapper mapper;

    @EventListener
    @Transactional
    public void onOffersExpired(@Valid final OffersExpiredEvent event) {
        final List<Stats> statsToSafe = new ArrayList<>();
        statsToSafe.add(
                new Stats(StatsKey.OFFERS_EXPIRED_PUBLIC_PART, event.numberOfPublicParts())
        );
        statsToSafe.add(
                new Stats(StatsKey.OFFERS_EXPIRED_PUBLIC_PART, event.numberOfPrivateParts())
        );
        repository.saveAll(statsToSafe);
    }

    @EventListener
    @Transactional
    public void onOffersDeleted(@Valid final OffersDeletedEvent event) {
        final List<Stats> statsToSafe = new ArrayList<>();
        statsToSafe.add(
                new Stats(StatsKey.OFFERS_DELETED_PUBLIC_PARTS, event.numberOfPublicParts())
        );
        statsToSafe.add(
                new Stats(StatsKey.OFFERS_DELETED_PRIVATE_PARTS, event.numberOfPrivateParts())
        );
        repository.saveAll(statsToSafe);
    }

    @Transactional
    public void processStats() {
        try {
            advisoryLockService.lock(
                    ModuleLockNamespace.STATS,
                    StatsAdvisoryLock.STATS_PROCESSING.name()
            );

            final List<StatsDto> statsDtos = offerService.retrieveStats(StatsKey.values());
            final List<Stats> statsEntity = mapper.mapList(statsDtos);

            repository.saveAll(statsEntity);
        } catch (Exception e) {
            log.error("Error while processing stats: " + e.getMessage(), e);
        }
    }
}
