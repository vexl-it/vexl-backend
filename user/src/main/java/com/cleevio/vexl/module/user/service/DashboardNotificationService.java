package com.cleevio.vexl.module.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardNotificationService {
    @Value("${dashboard.new-user-hook:@null}")
    private final String newUserHook;

    private final RestTemplate restTemplate;

    @Async("dashboardHooksTaskExecutor")
    public void sendNoticeOnNewUserCreated() {
        log.info("Sending notification to dashboard service");

        if(this.newUserHook.equals("@null")) {
            log.info("No hook provided, skipping notification");
            return;
        }

        restTemplate.postForEntity(this.newUserHook, null, Void.class);
        log.info("Notification sent");
    }
}
