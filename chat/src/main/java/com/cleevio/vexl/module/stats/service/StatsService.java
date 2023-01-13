package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.constant.StatsAdvisoryLock;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.stats.entity.Stats;
import com.cleevio.vexl.module.stats.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AdvisoryLockService advisoryLockService;
    private final StatsRepository repository;
    private final MessageService messageService;
    private final StatsMapper mapper;

    @Transactional
    public void processStats() {
        try {
            advisoryLockService.lock(
                    ModuleLockNamespace.STATS,
                    StatsAdvisoryLock.STATS_PROCESSING.name()
            );

            final List<StatsDto> statsDtos = messageService.retrieveStats(StatsKey.values());
            final List<Stats> statsEntity = mapper.mapList(statsDtos);

            repository.saveAll(statsEntity);
        } catch (Exception e) {
            log.error("Failed to process stats: " + e.getMessage(), e);
        }
    }
}
