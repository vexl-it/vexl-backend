package com.cleevio.vexl.module.stats.service.scheduled;

import com.cleevio.vexl.module.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsTask {

    private final StatsService statsService;

    @Scheduled(cron = "-")
    public void processStats() {
        statsService.processStats();
    }
}
