package com.cleevio.vexl.module.user.service.scheduled;

import com.cleevio.vexl.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInactivityTask {

    private final UserService userService;

    @Value("${inactivity.period}")
    private final Integer notificationAfter;

    @Scheduled(cron = "${cron.inactivity}")
    public void processUserInactivity() {
        this.userService.processNotificationsForInactivity(notificationAfter);
    }
}
