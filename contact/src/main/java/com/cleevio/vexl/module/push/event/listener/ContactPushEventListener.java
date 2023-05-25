package com.cleevio.vexl.module.push.event.listener;

import com.cleevio.vexl.module.contact.dto.request.ContactsImportedEvent;
import com.cleevio.vexl.module.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class ContactPushEventListener {

    private final PushService pushService;

    @EventListener
    public void onContactsImportedEvent(@Valid final ContactsImportedEvent event) {
        this.pushService.sendImportedNotification(
                event.firebaseTokens(),
                event.firebaseTokensSecondDegree(),
                event.newUserPublicKey()
        );
    }

}
