package com.cleevio.vexl.module.push.event.listener;

import com.cleevio.vexl.module.push.service.PushService;
import com.cleevio.vexl.module.user.event.NewContentNotificationEvent;
import com.cleevio.vexl.module.user.event.UserInactivityLimitExceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class UserPushEventListener {

    private final PushService pushService;

    @EventListener
    public void onUserInactivityLimitExceededEvent(@Valid final UserInactivityLimitExceededEvent event) {
        pushService.sendInactivityReminderNotification(event.inactivityNotificationDtos());
    }

    @EventListener
    public void onNotifyUsersAboutNewContent(@Valid final NewContentNotificationEvent event) {
        pushService.sendNewContentNotification(event.dtos());
    }
}
