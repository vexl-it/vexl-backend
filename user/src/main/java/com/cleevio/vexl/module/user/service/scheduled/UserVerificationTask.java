package com.cleevio.vexl.module.user.service.scheduled;

import com.cleevio.vexl.module.user.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Task for deleting expired verifications.
 * Every 10 minutes we check if there is some expired verification in USER_VERIFICATION table, if so, we delete it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserVerificationTask {

    private final UserVerificationService verificationService;

    @Scheduled(fixedDelay = 600_000)
    public void deleteExpiredVerifications() {
        log.info("Deleting expired user verifications");
        this.verificationService.deleteExpiredVerifications();
    }
}
