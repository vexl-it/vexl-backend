package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.stats.constant.StatsAdvisoryLock;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.stats.entity.Stats;
import com.cleevio.vexl.module.stats.mapper.StatsMapper;
import com.cleevio.vexl.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AdvisoryLockService advisoryLockService;
    private final StatsRepository repository;
    private final ContactService contactService;
    private final UserService userService;
    private final StatsMapper mapper;

    @Transactional
    public void processStats() {
        try {
            advisoryLockService.lock(
                    ModuleLockNamespace.STATS,
                    StatsAdvisoryLock.STATS_PROCESSING.name()
            );

            final List<StatsDto> statsDtos = contactService.retrieveStats(StatsKey.values());
            statsDtos.addAll(userService.retrieveStats(StatsKey.values()));
            final List<Stats> statsEntity = mapper.mapList(statsDtos);

            repository.saveAll(statsEntity);
        } catch (Exception e) {
            log.error("Error occurred during stats processing: " + e.getMessage(), e);
        }
    }
}
