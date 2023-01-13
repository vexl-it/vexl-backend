package com.cleevio.vexl.module.push.service.scheduled;

import com.cleevio.vexl.module.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushTask {

    private final PushService pushService;

    @Scheduled(fixedDelay = 60_000)
    public void processPushNotification() {
        pushService.processPushNotification();
    }
}
