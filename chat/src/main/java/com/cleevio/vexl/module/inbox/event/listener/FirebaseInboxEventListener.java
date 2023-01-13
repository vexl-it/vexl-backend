package com.cleevio.vexl.module.inbox.event.listener;

import com.cleevio.vexl.common.integration.firebase.event.FirebaseTokenInvalidatedEvent;
import com.cleevio.vexl.module.inbox.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class FirebaseInboxEventListener {

    private final InboxService inboxService;

    @Async
    @EventListener
    public void onFirebaseTokenInvalidatedEvent(@Valid FirebaseTokenInvalidatedEvent event) {
        this.inboxService.deleteInvalidToken(event.inboxPublicKey(), event.firebaseToken());
    }

}
