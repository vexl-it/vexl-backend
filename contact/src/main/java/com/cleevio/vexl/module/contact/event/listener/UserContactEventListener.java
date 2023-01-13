package com.cleevio.vexl.module.contact.event.listener;

import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.user.event.UserRemovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class UserContactEventListener {

    private final ContactService contactService;

    @EventListener
    public void onUserRemovedEvent(@Valid final UserRemovedEvent event) {
        this.contactService.deleteAllContacts(event.publicKey());
    }
}
