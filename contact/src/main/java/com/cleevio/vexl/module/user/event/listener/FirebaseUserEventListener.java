package com.cleevio.vexl.module.user.event.listener;

import com.cleevio.vexl.common.integration.firebase.event.FirebaseTokenUnregisteredEvent;
import com.cleevio.vexl.common.integration.firebase.event.InactivityNotificationSuccessfullySentEvent;
import com.cleevio.vexl.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class FirebaseUserEventListener {

    private final UserService userService;

    @Async
    @EventListener
    public void onFirebaseTokenUnregisteredEvent(@Valid FirebaseTokenUnregisteredEvent event) {
        this.userService.deleteUnregisteredToken(event.firebaseToken());
    }

    @Async
    @EventListener
    public void onInactivityNotificationSuccessfullySentEvent(@Valid InactivityNotificationSuccessfullySentEvent event) {
        this.userService.resetDatesForNotifiedUsers(event.firebaseTokens());
    }
}
